import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;
import java.util.stream.Collectors;

public class ChronologicalTableau implements Tableau{

    private final List<OWLClassExpression> conceptList;

    private int workingRule = 0;

    private final Map<OWLObjectPropertyExpression, List<Integer>> someRelation;

    private final Map<OWLObjectPropertyExpression, List<Integer>> allRelation;

    private final int parent;

    Comparator<? super OWLClassExpression> conceptComparator;

    protected ChronologicalTableau(OWLClassExpression concept, int parent) {

        conceptList = new ArrayList<>();
        conceptList.add(0, concept);
        someRelation = new HashMap<>();
        allRelation = new HashMap<>();
        this.parent = parent;
        conceptComparator = (Comparator<OWLClassExpression>) (expression, t1) -> {

            ClassExpressionType type = expression.getClassExpressionType();
            switch (type) {
                case OBJECT_INTERSECTION_OF:
                    if (t1.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
                        return 0;
                    }
                    return -1;

                case OBJECT_UNION_OF:
                    switch (t1.getClassExpressionType()) {
                        case OBJECT_INTERSECTION_OF:
                            return 1;
                        case OBJECT_UNION_OF:
                            return 0;
                        default:
                            return -1;
                    }
                case OBJECT_SOME_VALUES_FROM:
                    switch (t1.getClassExpressionType()) {
                        case OBJECT_INTERSECTION_OF:
                        case OBJECT_UNION_OF:
                            return 1;
                        case OBJECT_SOME_VALUES_FROM:
                            return 0;
                        default:
                            return -1;
                    }

                case OBJECT_ALL_VALUES_FROM:
                    switch (t1.getClassExpressionType()) {
                        case OBJECT_INTERSECTION_OF:
                        case OBJECT_UNION_OF:
                        case OBJECT_SOME_VALUES_FROM:
                            return 1;
                        case OBJECT_ALL_VALUES_FROM:
                            return 0;
                        default:
                            return -1;
                    }
                case OWL_CLASS:
                case OBJECT_COMPLEMENT_OF:
                    switch (t1.getClassExpressionType()) {
                        case OWL_CLASS:
                        case OBJECT_COMPLEMENT_OF:
                            return 0;
                        default:
                            return 1;
                    }

            }
            return -1;
        };

        LoggerManager.writeDebugLog("SAT: "+ parent, ChronologicalTableau.class);

    }

    @Override
    public boolean SAT() {

        if(isWorking()) {
            OWLClassExpression rule = conceptList.get(workingRule);
            ClassExpressionType type = rule.getClassExpressionType();
            switch (type) {
                case OBJECT_INTERSECTION_OF:
                    return applyIntersection();
                case OBJECT_UNION_OF:
                    return applyUnion();
                case OBJECT_SOME_VALUES_FROM:
                    return applySome(rule);
                case OBJECT_ALL_VALUES_FROM:
                    return applyAll(rule);
                case OWL_CLASS:
                case OBJECT_COMPLEMENT_OF:
                    LoggerManager.writeDebugLog("Rule: "+ workingRule + " CLASS :" + OntologyRenderer.render(conceptList.get(workingRule)), ChronologicalTableau.class);
                    if (checkClash()) {
                        conceptList.remove(workingRule);
                        workingRule--;
                        return false;
                    }
                    workingRule++;
                    return SAT();
            }

        }
        return true;

    }

    private boolean applyIntersection(){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(conceptList.get(workingRule)), ChronologicalTableau.class);


        OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) conceptList.get(workingRule);
        List<OWLClassExpression> operand = intersection.operands().sorted(conceptComparator).collect(Collectors.toList());
        for (OWLClassExpression owlClassExpression : operand) {
            if (!conceptList.contains(owlClassExpression))
                conceptList.add(conceptList.size(), owlClassExpression);
        }
        workingRule ++;
        return SAT();
    }

    private boolean applyUnion(){
        LoggerManager.writeDebugLog("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(conceptList.get(workingRule)), ChronologicalTableau.class);

        int rule = workingRule;

        OWLObjectUnionOf union = (OWLObjectUnionOf) conceptList.get(workingRule);
        List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
        ArrayList<OWLClassExpression> saveT = new ArrayList<>(conceptList);

        jointedList.sort(conceptComparator);

        /*if(!branchingNode.contains(rule))
            branchingNode.add(branchingNode.size(),rule);*/

        for (OWLClassExpression owlClassExpression : jointedList) {

            if (!conceptList.contains(owlClassExpression)) {
                LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(owlClassExpression), ChronologicalTableau.class);

                conceptList.add(conceptList.size(), owlClassExpression);
                /*if(i == jointedList.size()-1)
                    branchingNode.remove(rule);*/
                if (checkClash())
                    conceptList.remove(conceptList.size() - 1);
                else {

                    workingRule++;
                    if (SAT())
                        return true;
                    else {
                        LoggerManager.writeDebugLog("BACKTRACK " + rule, ChronologicalTableau.class);

                        workingRule = rule;
                        cleanRelation(someRelation);
                        cleanRelation(allRelation);
                        conceptList.removeAll(Collections.unmodifiableList(conceptList));
                        conceptList.addAll(saveT);

                    }
                }
            }
        }


        return false;

    }

    private boolean applySome(OWLClassExpression rule){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(rule), ChronologicalTableau.class);

        Tableau direct;
        OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();

        List<Integer> related = new ArrayList<>();
        //VERIFICO SE INDIVIDUO HA LA RELAZIONE QUESTO
        if(someRelation.get(oe)!=null)
            related.addAll(someRelation.get(oe));

        //CASO IN CUI RELAZIONE RICHIESTA ESISTE, VERIFICO SE E' PRESENTE LA REGOLA NELla CONCEPT LIST
        if (related.size()!=0) {

            OWLObjectSomeValuesFrom flag;
            for (Integer r : related) {

                flag = (OWLObjectSomeValuesFrom) conceptList.get(r);

                if(filler.equals(flag.getFiller())){
                    LoggerManager.writeDebugLog("SOME ALREADY PRESENT", ChronologicalTableau.class);

                    workingRule++;
                    return SAT();

                }
            }
        }
        //CASO IN CUI INDIVIDUO O NON HA LA RELAZIONE O
        //NESSUNO DEI INDIVIDUI CON QUESTA RELAZIONE HA LA FORMULA TRA LA SUA CONCEPT LIST
        //QUINDI INSTANZIO NUOVO INDIVIDUO E MI SALVO LA RELAZIONE

        if(allRelation.get(oe)!=null){

            //CREO INTERSEZIONE DEI CONCETTI CONTENUTI NEGLI ESISTENZIALI DI QUESTO TIPO

            OWLObjectAllValuesFrom allRule;

            ArrayList<OWLClassExpression> operands = new ArrayList<>();

            for (Integer i: allRelation.get(oe)) {

                allRule = (OWLObjectAllValuesFrom)conceptList.get(i);
                operands.add(allRule.getFiller());

            }

            operands.add(filler);
            operands.sort(conceptComparator);
            filler = new OWLObjectIntersectionOfImpl(operands);

        }

        direct = new ChronologicalTableau(filler, workingRule);
        if(direct.SAT()) {
            LoggerManager.writeDebugLog("SOME "+workingRule+" SATISFIABLE", ChronologicalTableau.class);

            related.add(related.size(),workingRule);
            someRelation.put(oe, related);
            workingRule++;
            return SAT();

        }
        else{
            LoggerManager.writeDebugLog("SOME UNSATISFIABLE", ChronologicalTableau.class);

            return false;

        }

    }

    private boolean applyAll(OWLClassExpression rule){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(rule), ChronologicalTableau.class);

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();

        if (someRelation.get(oe) == null){
            LoggerManager.writeDebugLog("ALL NO CONDITIONS", ChronologicalTableau.class);

            if(allRelation.get(oe) == null)
                allRelation.put(oe,Collections.singletonList(workingRule));
            else{

                ArrayList<Integer> l = new ArrayList<>(allRelation.get(oe));
                l.add(l.size(),workingRule);
                allRelation.put(oe,l);

            }

        }
        else{

            ArrayList<Integer> related = new ArrayList<>(someRelation.get(oe));
            ArrayList<OWLClassExpression> allRules = new ArrayList<>();
            OWLObjectSomeValuesFrom flag;

            if(allRelation.get(oe)!=null){

                OWLObjectAllValuesFrom allRule;

                for (Integer j: allRelation.get(oe)) {

                    allRule = (OWLObjectAllValuesFrom) conceptList.get(j);
                    allRules.add(allRule.getFiller());

                }

                allRules.sort(conceptComparator);

            }

            for (Integer integer : related) {

                flag = (OWLObjectSomeValuesFrom) conceptList.get(integer);

                if (!filler.equals(flag.getFiller())) {

                    ArrayList<OWLClassExpression> operands = new ArrayList<>();
                    operands.add(flag.getFiller());
                    operands.add(filler);
                    operands.addAll(allRules);
                    operands.sort(conceptComparator);

                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);
                    Tableau Tflag = new ChronologicalTableau(concept.getNNF(), workingRule);

                    if (!Tflag.SAT()) {
                        LoggerManager.writeDebugLog("ALL UNSATISFIABLE", ChronologicalTableau.class);

                        return false;

                    }
                }
            }

        }
        workingRule++;
        return SAT();

    }


    private boolean isWorking() {
        return !((workingRule>= conceptList.size()) || (workingRule<0));

    }

    private void cleanRelation(Map<OWLObjectPropertyExpression, List<Integer>> relation){
        Set<OWLObjectPropertyExpression> list = relation.keySet();
        for (OWLObjectPropertyExpression oe : list) {

            ArrayList<Integer> t = new ArrayList<>(relation.remove(oe));


            for (int i = t.size() - 1; i >=0 ; i--) {
                if(t.get(i) > workingRule)
                    t.remove(i);
            }

            if(t.size()!=0)
                relation.put(oe,t);

        }

    }

    private boolean checkClash() {

        for (int i = 0; i < conceptList.size(); i++) {

            OWLClassExpression c = conceptList.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < conceptList.size(); i1++) {

                OWLClassExpression c1 = conceptList.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    LoggerManager.writeDebugLog("CLASH "+ OntologyRenderer.render(c) + " " +OntologyRenderer.render(c1), ChronologicalTableau.class);
                    return true;
                }
            }
        }
        return false;

    }

    @Override
    public String getModel(){return null;}
    /*public String getModel(){
        String model = "| ";
        for (OWLClassExpression e: conceptList) {
            if(e != null) {
                ClassExpressionType pe = e.getClassExpressionType();
                switch (pe) {
                    case OWL_CLASS:
                    case OBJECT_COMPLEMENT_OF:

                        model=model.concat(OntologyRenderer.render((e))+ " | ");

                        break;
                }
            }
        }
        if(!someRelation.isEmpty()) {
            Set<OWLObjectPropertyExpression> key = someRelation.keySet();
            for (OWLObjectPropertyExpression oe : key) {
                if (oe != null) {
                    List<Integer> related = someRelation.get(oe);

                    for (Integer j : related) {
                        Tableau t = nodeList.get(j);
                        model=model.concat("EXIST " + OntologyRenderer.render((oe)) + ". { ");
                        model = model.concat(t.getModel());
                        if(model.chars().filter(ch -> ch == '}').count() < model.chars().filter(ch -> ch == '{').count())
                            model=model.concat("} | ");
                    }
                }
            }

        }
        if(!allRelation.isEmpty()) {
            Set<OWLObjectPropertyExpression> key = allRelation.keySet();
            for (OWLObjectPropertyExpression oe : key) {
                if (oe != null) {
                    List<Integer> related = allRelation.get(oe);

                    for (Integer j : related) {
                        Tableau t = nodeList.get(j);
                        if(t.getIteration()!=0) {
                            model=model.concat("EXIST " + OntologyRenderer.render((oe)) + ". { ");
                            model = model.concat(t.getModel());
                            if(model.chars().filter(ch -> ch == '}').count() < model.chars().filter(ch -> ch == '{').count())
                                model=model.concat("} | ");
                        }
                    }
                }
            }
        }


        return model;
    }*/

    @Override
    public Integer getIteration(){return null;}
    /*public Integer getIteration(){


        int it=iteration;

        Set<OWLObjectPropertyExpression> listSome = someRelation.keySet();

        for (OWLObjectPropertyExpression oe : listSome) {

            ArrayList<Integer> lt = new ArrayList<>(someRelation.get(oe));

            for (Integer t: lt) {
                Tableau m = nodeList.get(t);
                it+=m.getIteration();

            }
        }

        Set<OWLObjectPropertyExpression> listAll = allRelation.keySet();

        for (OWLObjectPropertyExpression oe : listAll) {

            ArrayList<Integer> lt = new ArrayList<>(allRelation.get(oe));

            for (Integer t: lt) {
                Tableau m = nodeList.get(t);
                it+=m.getIteration();

            }
        }
        return it;
    }*/

    @Override
    public int getParent() {
        return parent;
    }

    @Override
    public List<OWLClassExpression> getConceptList() {
        return conceptList;
    }
}




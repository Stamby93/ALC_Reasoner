import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;
import java.util.stream.Collectors;

public class JumpingTableau implements Tableau{

    private final List<OWLClassExpression> conceptList;

    private final List<List<Integer>> dependency;

    private List<Integer> clashList;

    private final Comparator<? super OWLClassExpression> conceptComparator;

    private final Map<OWLObjectPropertyExpression, List<Integer>> someRelation;

    private final Map<OWLObjectPropertyExpression, List<Integer>> allRelation;

    private int workingRule = 0;

    private int iteration = 0;

    protected JumpingTableau(OWLClassExpression concept, int parent) {

        conceptList = new ArrayList<>();
        conceptList.add(0, concept);
        dependency = new ArrayList<>();
        dependency.add(0,Collections.singletonList(-1));
        someRelation = new HashMap<>();
        allRelation = new HashMap<>();
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
        LoggerManager.writeDebugLog("SAT: "+ parent, JumpingTableau.class);

    }

    @Override
    public boolean SAT() {

        if(workingRule <= conceptList.size() -1) {
            OWLClassExpression rule = conceptList.get(workingRule);
            ClassExpressionType type = rule.getClassExpressionType();
            switch (type) {
                case OBJECT_INTERSECTION_OF:
                    return applyIntersection((OWLObjectIntersectionOf)rule);
                case OBJECT_UNION_OF:
                    return applyUnion((OWLObjectUnionOf)rule);
                case OBJECT_SOME_VALUES_FROM:
                    return applySome((OWLObjectSomeValuesFrom)rule);
                case OBJECT_ALL_VALUES_FROM:
                    return applyAll((OWLObjectAllValuesFrom)rule);
                case OWL_CLASS:
                case OBJECT_COMPLEMENT_OF:
                    LoggerManager.writeDebugLog("Rule: "+ workingRule + " CLASS :" + OntologyRenderer.render(conceptList.get(workingRule)), JumpingTableau.class);

                    iteration++;
                    if (checkClash())
                        return false;

                    workingRule++;
                    return SAT();
            }

        }
        return true;

    }

    private void addDependency(int start, int end, List<Integer> rule){

        for(int i = start; i < end; i++)
            dependency.add(i,new ArrayList<>(rule));

    }

    private boolean applyIntersection(OWLObjectIntersectionOf intersection){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(intersection), JumpingTableau.class);

        List<OWLClassExpression> operand = intersection.operands().sorted(conceptComparator).collect(Collectors.toList());
        int i = 0;
        for (OWLClassExpression owlClassExpression : operand) {
            if (!conceptList.contains(owlClassExpression)){
                conceptList.add(conceptList.size(), owlClassExpression);
                i++;
            }
        }
        if(i!=0)
            addDependency(conceptList.size() - i,conceptList.size() , dependency.get(workingRule));
        iteration++;
        workingRule ++;
        return SAT();
    }

    private boolean applyUnion(OWLObjectUnionOf union){
        LoggerManager.writeDebugLog("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(union), JumpingTableau.class);

        int rule = workingRule;
        List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
        ArrayList<OWLClassExpression> saveT = new ArrayList<>(conceptList);
        ArrayList<List<Integer>> saveTD = new ArrayList<>(dependency);
        ArrayList<Integer> dep = new ArrayList<>();
        dep.add(workingRule);
        OWLClassExpression owlClassExpression;

        jointedList.sort(conceptComparator);

        for (int i = 0; i < jointedList.size(); i++) {

            iteration++;
            owlClassExpression = jointedList.get(i);

            if (!conceptList.contains(owlClassExpression)) {
                LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(owlClassExpression), JumpingTableau.class);

                conceptList.add(conceptList.size(), owlClassExpression);

                addDependency(conceptList.size() - 1, conceptList.size(), dep);

                if (checkClash()){

                    conceptList.remove(conceptList.size() - 1);
                    dependency.remove(dependency.size()-1);
                    for (Integer c: clashList ) {

                        if(!dep.contains(c))
                            dep.add(c);

                    }

                    Collections.sort(dep);

                }
                else {

                    workingRule++;
                    if (i == jointedList.size()-1)
                        return SAT();

                    if(SAT())
                        return true;
                    else if(!clashList.contains(rule))
                        return false;

                    LoggerManager.writeDebugLog("BACKTRACK: " + rule, JumpingTableau.class);

                    workingRule = rule;
                    cleanRelation(someRelation);
                    cleanRelation(allRelation);
                    conceptList.removeAll(Collections.unmodifiableList(conceptList));
                    conceptList.addAll(saveT);
                    dependency.removeAll(Collections.unmodifiableList(dependency));
                    dependency.addAll(saveTD);

                }
            }
        }

        clashList.remove(Integer.valueOf(rule));

        return false;

    }

    private boolean applySome(OWLObjectSomeValuesFrom someValue){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(someValue), JumpingTableau.class);

        Tableau direct;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();
        clashList = new ArrayList<>();

        List<Integer> related = new ArrayList<>();
        //VERIFICO SE INDIVIDUO HA LA RELAZIONE QUESTO
        if(someRelation.get(oe)!=null)
            related.addAll(someRelation.get(oe));

        //CASO IN CUI RELAZIONE RICHIESTA ESISTE, VERIFICO SE E' PRESENTE LA REGOLA NELLA CONCEPT LIST
        if (related.size()!=0) {

            OWLObjectSomeValuesFrom flag;
            for (Integer r : related) {

                flag = (OWLObjectSomeValuesFrom) conceptList.get(r);

                if(filler.equals(flag.getFiller())){
                    LoggerManager.writeDebugLog("SOME ALREADY PRESENT", JumpingTableau.class);

                    workingRule++;
                    iteration ++;
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
                for (Integer d: dependency.get(i)) {

                    if(!clashList.contains(d))
                        clashList.add(d);

                }
            }

            operands.add(filler);
            operands.sort(conceptComparator);
            filler = new OWLObjectIntersectionOfImpl(operands);

        }

        direct = new JumpingTableau(filler, workingRule);
        if(direct.SAT()) {
            LoggerManager.writeDebugLog("SOME "+workingRule+" SATISFIABLE", JumpingTableau.class);

            related.add(related.size(),workingRule);
            someRelation.put(oe, related);
            workingRule++;
            iteration += direct.getIteration();
            return SAT();

        }
        else{
            LoggerManager.writeDebugLog("SOME UNSATISFIABLE", JumpingTableau.class);

            iteration += direct.getIteration();
            for (Integer d: dependency.get(workingRule)) {

                if(!clashList.contains(d))
                    clashList.add(d);

            }
            Collections.sort(clashList);
            return false;

        }

    }

    private boolean applyAll(OWLObjectAllValuesFrom allValue){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(allValue), JumpingTableau.class);

        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();

        if (someRelation.get(oe) == null){
            LoggerManager.writeDebugLog("ALL NO CONDITIONS", JumpingTableau.class);

            iteration++;

        }
        else{

            ArrayList<Integer> related = new ArrayList<>(someRelation.get(oe));
            ArrayList<OWLClassExpression> allRules = new ArrayList<>();
            OWLObjectSomeValuesFrom flag;
            clashList = new ArrayList<>();

            allRules.add(filler);

            if(allRelation.get(oe)!=null){

                OWLObjectAllValuesFrom allRule;

                for (Integer j: allRelation.get(oe)) {

                    allRule = (OWLObjectAllValuesFrom) conceptList.get(j);
                    allRules.add(allRule.getFiller());
                    for (Integer d: dependency.get(j)) {

                        if(!clashList.contains(d))
                            clashList.add(d);

                    }

                }

                allRules.sort(conceptComparator);

            }

            for (Integer integer : related) {

                flag = (OWLObjectSomeValuesFrom) conceptList.get(integer);

                if (!filler.equals(flag.getFiller())) {

                    ArrayList<OWLClassExpression> operands = new ArrayList<>();
                    operands.add(flag.getFiller());
                    operands.addAll(allRules);
                    operands.sort(conceptComparator);

                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);
                    Tableau Tflag = new JumpingTableau(concept, workingRule);

                    if (!Tflag.SAT()) {
                        LoggerManager.writeDebugLog("ALL UNSATISFIABLE", JumpingTableau.class);

                        iteration += Tflag.getIteration();

                        for (Integer d: dependency.get(workingRule)) {

                            if(!clashList.contains(d))
                                clashList.add(d);

                        }

                        for (Integer d: dependency.get(integer)) {

                            if(!clashList.contains(d))
                                clashList.add(d);

                        }

                        Collections.sort(clashList);

                        return false;

                    }
                    LoggerManager.writeDebugLog("ALL "+workingRule+" SATISFIABLE", ChronologicalTableau.class);


                    iteration += Tflag.getIteration();

                }
            }
        }

        if(allRelation.get(oe) == null)
            allRelation.put(oe,Collections.singletonList(workingRule));
        else{

            ArrayList<Integer> l = new ArrayList<>(allRelation.get(oe));
            l.add(l.size(),workingRule);
            allRelation.put(oe,l);

        }

        workingRule++;
        return SAT();

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

        clashList = new ArrayList<>();

        for (int i = 0; i < conceptList.size(); i++) {

            OWLClassExpression c = conceptList.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < conceptList.size(); i1++) {

                OWLClassExpression c1 = conceptList.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    LoggerManager.writeDebugLog("CLASH "+ OntologyRenderer.render(c) + " " +OntologyRenderer.render(c1), JumpingTableau.class);
                    clashList.addAll(dependency.get(i));
                    for (Integer d: dependency.get(i1)) {

                        if(!clashList.contains(d))
                            clashList.add(d);

                    }
                    Collections.sort(clashList);
                    return true;
                }
            }
        }
        return false;

    }

    @Override
    public String getModel(){
        String model = "| ";
        for (OWLClassExpression e: conceptList) {
            if(e != null) {
                ClassExpressionType pe = e.getClassExpressionType();
                switch (pe) {
                    case OWL_CLASS:
                    case OBJECT_COMPLEMENT_OF:
                    case OBJECT_SOME_VALUES_FROM:
                    case OBJECT_ALL_VALUES_FROM:

                        model=model.concat(OntologyRenderer.render((e))+ " | ");

                        break;
                }
            }
        }

        return model;
    }

    @Override
    public Integer getIteration(){return iteration;}

}




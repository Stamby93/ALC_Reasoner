import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;

public class ChronologicalTableau implements Tableau{

    private final List<OWLClassExpression> conceptList;

    private final List<Integer> branchingNode;

    private final List<Tableau> nodeList;

    private int workingRule = 0;

    private int workingNode = 0;

    private final Map<OWLObjectPropertyExpression, List<Integer>> someRelation;

    private final Map<OWLObjectPropertyExpression, List<Integer>> allRelation;

    private final int parent;

    private int iteration=0;

    protected ChronologicalTableau(OWLClassExpression concept, int parent) {

        conceptList = new ArrayList<>();
        conceptList.add(0, concept);
        branchingNode = new ArrayList<>();
        someRelation = new HashMap<>();
        allRelation = new HashMap<>();
        nodeList = new ArrayList<>();
        this.parent = parent;
    }

    @Override
    public boolean SAT() {

        LoggerManager.writeDebugLog("SAT: "+ parent, ChronologicalTableau.class);

        while(isWorking()){


            OWLClassExpression rule = conceptList.get(workingRule);
            ClassExpressionType type = rule.getClassExpressionType();
            switch (type) {
                case OBJECT_INTERSECTION_OF:
                    applyIntersection();
                    break;
                case OBJECT_UNION_OF:
                    applyUnion();
                    break;
                case OBJECT_SOME_VALUES_FROM:
                    applySome(rule);
                    break;
                case OBJECT_ALL_VALUES_FROM:
                    applyAll(rule);
                    break;
                case OWL_CLASS:
                case OBJECT_COMPLEMENT_OF:

                    LoggerManager.writeDebugLog("CLASS :"+ OntologyRenderer.render(conceptList.get(workingRule)), ChronologicalTableau.class);
                    if(checkClash()){
                        workingNode--;
                        backtrack();
                    }
                    else
                        workingRule++;
                    break;
            }
            iteration++;
        }

        LoggerManager.writeDebugLog("SAT: "+ parent+ " " + ((workingRule >= 0) & (workingNode >= 0)), ChronologicalTableau.class);

        if (parent==-1 && ((workingRule >= 0) & (workingNode >= 0))){
            LoggerManager.writeDebugLog("NUMERO ITERAZIONI: " + getIteration(), JumpingTableau.class);
            LoggerManager.writeDebugLog("MODELLO: " + getModel(), JumpingTableau.class);

        }


        return ((workingRule >= 0) & (workingNode >= 0));

    }

    private void applyIntersection(){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(conceptList.get(workingRule)), ChronologicalTableau.class);
        Tableau Node = new Node(conceptList, workingRule);
        Node.SAT();
        nodeList.add(workingNode,Node);
        if(conceptList.size() != Node.getConceptList().size()){
            conceptList.removeAll(Collections.unmodifiableList(conceptList));
            conceptList.addAll(Node.getConceptList());
        }
        workingRule ++;
        workingNode ++;
    }

    private void applyUnion(){
        LoggerManager.writeDebugLog("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(conceptList.get(workingRule)), ChronologicalTableau.class);

        Tableau Node;

        if(branchingNode.contains(workingNode))
            Node = nodeList.get(workingNode);
        else{
            Node = new Node(conceptList, workingRule);
            branchingNode.add(branchingNode.size(),workingNode);
            nodeList.add(workingNode,Node);
        }

        boolean haveChoice = false;
        while(Node.SAT()){

            if(conceptList.size() != Node.getConceptList().size()){
                ArrayList<OWLClassExpression> saveT = new ArrayList<>(conceptList);
                conceptList.removeAll(Collections.unmodifiableList(conceptList));
                conceptList.addAll(Node.getConceptList());

                LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(conceptList.get(conceptList.size()-1)), ChronologicalTableau.class);

                if(checkClash()){
                    conceptList.removeAll(Collections.unmodifiableList(conceptList));
                    conceptList.addAll(saveT);
                    iteration++;
                }
                else{
                    workingRule++;
                    workingNode++;
                    haveChoice = true;
                    break;
                }
            }
            else{
                workingRule++;
                workingNode++;
                haveChoice = true;
                break;
            }
        }

        if(!haveChoice){
            branchingNode.remove(Integer.valueOf(workingNode));
            backtrack();
        }

    }

    private void applySome(OWLClassExpression rule){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(rule), ChronologicalTableau.class);

        Tableau direct;
        OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();

        List<Integer> related;
        //VERIFICO SE INDIVIDUO HA LA RELAZIONE QUESTO
        if(someRelation.get(oe)!=null)
            related = new ArrayList<>(someRelation.get(oe));
        else
            related = new ArrayList<>();

        boolean condition = true;

        //CASO IN CUI RELAZIONE RICHIESTA ESISTE, VERIFICO SE E' PRESENTE LA REGOLA NEL RULE SET
        if (related.size()!=0) {

            for (Integer r : related) {

                direct = nodeList.get(r);

                if (direct.getConceptList().contains(filler)) {

                    LoggerManager.writeDebugLog("SOME ALREADY PRESENT", ChronologicalTableau.class);
                    condition = false;
                    workingRule++;

                    break;
                }
            }
        }
        //CASO IN CUI INDIVIDUO O NON HA LA RELAZIONE O
        //NESSUNO DEI INDIVIDUI CON QUESTA RELAZIONE HA LA FORMULA TRA IL SUO RULE SET
        //QUINDI INSTANZIO NUOVO INDIVIDUO E MI SALVO LA RELAZIONE
        if (condition) {

            if(allRelation.get(oe)!=null){

                ArrayList<OWLClassExpression> operands = new ArrayList<>();

                for (Integer i: allRelation.get(oe)) {

                    direct = nodeList.get(i);
                    operands.add(direct.getConceptList().get(0));

                }

                operands.add(filler);
                filler = new OWLObjectIntersectionOfImpl(operands);

            }

            direct = new ChronologicalTableau(filler, workingRule);
            nodeList.add(workingNode,direct);
            if(direct.SAT()) {

                related.add(related.size(),workingNode);
                someRelation.put(oe, related);
                workingNode++;
                workingRule++;
            }
            else{

                LoggerManager.writeDebugLog("SOME UNSATISFIABLE", ChronologicalTableau.class);
                backtrack();

            }
        }
    }

    private void applyAll(OWLClassExpression rule){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(rule), ChronologicalTableau.class);

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        if (someRelation.get(oe) == null){
            LoggerManager.writeDebugLog("ALL NO CONDITIONS", ChronologicalTableau.class);

            Tableau t = new ChronologicalTableau(filler.getNNF(),workingRule);
            nodeList.add(workingNode,t);

            if(allRelation.get(oe) == null)
                allRelation.put(oe,Collections.singletonList(workingNode));
            else{

                ArrayList<Integer> l = new ArrayList<>(allRelation.get(oe));
                l.add(l.size(),workingNode);
                allRelation.put(oe,l);

            }

            workingNode++;
            workingRule++;

        }
        else{

            ArrayList<Integer> newRelated = new ArrayList<>();
            ArrayList<Integer> related = new ArrayList<>(someRelation.get(oe));

            if(allRelation.get(oe)!=null){

                for (Integer j: allRelation.get(oe)) {
                    if(nodeList.get(j).getIteration()!=0)
                        related.add(related.size(),j);
                }
            }

            for (int i  = 0 ; (i < related.size()) && check; i++){

                Tableau t = nodeList.get(related.get(i));

                if(!t.getConceptList().contains(filler)){

                    ArrayList<OWLClassExpression> operands = new ArrayList<>();
                    operands.add(t.getConceptList().get(0));
                    operands.add(filler);
                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);
                    Tableau flag = new ChronologicalTableau(concept.getNNF(),workingRule);
                    nodeList.add(workingNode,flag);

                    if(!flag.SAT()){

                        LoggerManager.writeDebugLog("ALL UNSATISFIABLE", ChronologicalTableau.class);
                        backtrack();
                        check = false;

                    }
                    else{

                        newRelated.add(newRelated.size(),workingNode);
                        workingNode++;

                    }
                }
            }

            if(check){

                if(newRelated.size()!=0){

                    if(allRelation.get(oe)!=null) {
                        newRelated.addAll(allRelation.get(oe));
                        allRelation.remove(oe);
                    }

                    Collections.sort(newRelated);
                    allRelation.put(oe,newRelated);

                }

                workingRule++;

            }
        }
    }

    private void backtrack() {
        LoggerManager.writeDebugLog("BACKTRACK :" + workingNode, ChronologicalTableau.class);

        iteration++;

        if(isWorking()) {

            if(!branchingNode.contains(workingNode)) {

                nodeList.remove(workingNode);
                workingNode--;
                cleanRelation(someRelation);
                cleanRelation(allRelation);
                backtrack();
            }
            else{

                Tableau Node = nodeList.get(workingNode);
                int dim = Node.getConceptList().size();

                conceptList.removeAll(Collections.unmodifiableList(conceptList));
                conceptList.addAll(Node.getConceptList().subList(0,dim-2));
                workingRule = Node.getParent();

            }
        }
    }

    private boolean isWorking() {
        return !(((workingRule>= conceptList.size()) || (workingRule<0)) || (workingNode<0));

    }

    private void cleanRelation(Map<OWLObjectPropertyExpression, List<Integer>> relation){
        Set<OWLObjectPropertyExpression> list = relation.keySet();
        for (OWLObjectPropertyExpression oe : list) {

            ArrayList<Integer> t = new ArrayList<>(relation.remove(oe));

            if(t.size()!=0){

                for (int i = t.size() - 1; i >=0 ; i--) {
                    if(t.get(i) > workingNode)
                        t.remove(i);
                }

                if(t.size()!=0)
                    relation.put(oe,t);
            }
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
    public String getModel(){
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
                            model=model.concat("FORALL " + OntologyRenderer.render((oe)) + ". { ");
                            model = model.concat(t.getModel());
                            if(model.chars().filter(ch -> ch == '}').count() < model.chars().filter(ch -> ch == '{').count())
                                model=model.concat("} | ");
                        }
                    }
                }
            }
        }


        return model;
    }

    @Override
    public Integer getIteration(){


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
    }

    @Override
    public int getParent() {
        return parent;
    }

    @Override
    public List<OWLClassExpression> getConceptList() {
        return conceptList;
    }
}




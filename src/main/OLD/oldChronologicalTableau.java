import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;

/**
 * The type Old chronological tableau.
 */
public class oldChronologicalTableau implements Tableau{

    private final List<OWLClassExpression> Abox;

    private final List<Integer> branchingNode;

    private List<Integer> dependency;

    private final Map<Integer, oldNode> nodeList;

    private int workingRule = 0;

    private final HashMap<OWLObjectPropertyExpression, List<oldChronologicalTableau>> someRelation;

    private final int parent;

    private int iteration=0;

    /**
     * Instantiates a new Old chronological tableau.
     *
     * @param concept the concept
     * @param parent  the parent
     */
    protected oldChronologicalTableau(OWLClassExpression concept, int parent) {

        Abox = new ArrayList<>();
        Abox.add(0, concept);
        branchingNode = new ArrayList<>();
        someRelation = new HashMap<>();
        nodeList = new HashMap<>();
        dependency = new ArrayList<>();
        dependency.add(0,-1);
        this.parent = parent;
    }


    /**
     * Check some boolean.
     *
     * @param expression the expression
     * @return the boolean
     */
    public boolean checkSome(OWLClassExpression expression) {

        return Abox.contains(expression);

    }


    /**
     * Sat boolean.
     *
     * @return the boolean
     */
    @Override
    public boolean SAT() {

        LoggerManager.writeDebug("SAT: "+ parent, oldChronologicalTableau.class);

        while(isWorking()){


            OWLClassExpression rule = Abox.get(workingRule);
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
                    LoggerManager.writeDebug("CLASS :"+ OntologyRenderer.render(Abox.get(workingRule)), oldChronologicalTableau.class);
                    if(checkClash()){
                        workingRule = dependency.get(workingRule);
                        backtrack();
                    }
                    else
                        workingRule++;
                    break;
            }
        iteration++;
        }

        LoggerManager.writeDebug("SAT: "+ parent+ " " + (workingRule > 0), oldChronologicalTableau.class);
        if (parent==-1){
            LoggerManager.writeDebug("NUMERO ITERAZIONI: " + getIteration(), oldChronologicalTableau.class);
            LoggerManager.writeDebug("MODELLO: " + getModel(), oldChronologicalTableau.class);

        }


        return workingRule >= 0;

    }

    private void applyIntersection(){
        LoggerManager.writeDebug("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(Abox.get(workingRule)), oldChronologicalTableau.class);
        oldNode workingNode = new oldNode(Abox, workingRule);
        nodeList.put(workingRule,workingNode);

        List<OWLClassExpression> flag = workingNode.applyRule();
        Integer i = workingRule;
        workingRule = dependency.get(i);
        checkIntersection(flag);
        workingRule = i +1;
    }

    private void applyUnion(){
        LoggerManager.writeDebug("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(Abox.get(workingRule)), oldChronologicalTableau.class);
        oldNode workingNode;

        if(branchingNode.size()!=0 && branchingNode.contains(workingRule)) {
            workingNode = nodeList.get(workingRule);

        } else{
            workingNode = new oldNode(Abox, workingRule);
            branchingNode.add(branchingNode.size(),workingRule);

        }
        List<OWLClassExpression> choice = workingNode.applyRule();
        if(choice!=null && choice.get(0)!=null) {
            LoggerManager.writeDebug("CHOICE " + OntologyRenderer.render(choice.get(0)), oldChronologicalTableau.class);
            checkIntersection(choice);
            nodeList.put(workingRule, workingNode);
            if (!checkClash()){
                workingRule++;
            }
            else
                backtrack();
        }
        else{
            branchingNode.remove(Integer.valueOf(workingRule));
            workingRule--;
            backtrack();
        }
    }

    private void applySome(OWLClassExpression rule){
        LoggerManager.writeDebug("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(rule), oldChronologicalTableau.class);

        oldChronologicalTableau direct;
        OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();

        //VERIFICO SE INDIVIDUO HA LA RELAZIONE
        List<oldChronologicalTableau> related = someRelation.get(oe);
        direct = new oldChronologicalTableau(filler, workingRule);

        if (related != null && related.size()!=0) {
            //CASO IN CUI RELAZIONE RICHIESTA ESISTE, VERIFICO SE E' PRESENTE LA REGOLA NEL RULE SET
            boolean check = false;

            for (oldChronologicalTableau t : related) {

                if (t.checkSome(filler)) {

                    check = true;

                    break;
                }

            }
            if (!check) {
                //CASO IN CUI NESSUNO DEI NODI CON QUESTA RELAZIONE HA LA FORMULA TRA IL SUO RULE SET
                //QUINDI INSTANZIO NUOVO INDIVIDUO E MI SALVO LA RELAZIONE
                if(direct.SAT()) {

                    ArrayList<oldChronologicalTableau> flag = new ArrayList<>(someRelation.get(oe));
                    flag.add(direct);
                    someRelation.put(oe, flag);
                    workingRule++;
                }
                else{
                    LoggerManager.writeDebug("SOME UNSATISFIABLE", oldChronologicalTableau.class);
                    workingRule = dependency.get(workingRule);
                    backtrack();
                }
            } else{
                LoggerManager.writeDebug("SOME ALREADY PRESENT", oldChronologicalTableau.class);
                workingRule++;
            }


        }
        else{

            if(direct.SAT()){
                someRelation.put(oe, Collections.singletonList(direct));
                workingRule++;
            }
            else{
                LoggerManager.writeDebug("SOME UNSATISFIABLE", oldChronologicalTableau.class);

                workingRule = dependency.get(workingRule);

                backtrack();
            }
        }
    }

    private void applyAll(OWLClassExpression rule){
        LoggerManager.writeDebug("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(rule), oldChronologicalTableau.class);

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        if (someRelation.get(oe) == null){

            LoggerManager.writeDebug("ALL NO CONDITIONS", oldChronologicalTableau.class);
            workingRule++;
        }
        else{

            ArrayList<oldChronologicalTableau> related = new ArrayList<>(someRelation.get(oe));
            int j = related.size();
            for (int i  = 0; (i < j) && check; i++){

                oldChronologicalTableau t = related.get(i);
                if(!t.checkSome(filler)){

                    ArrayList<OWLClassExpression> operands = new ArrayList<>();
                    operands.add(t.getAbox().get(0));
                    operands.add(filler);
                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);

                    oldChronologicalTableau flag = new oldChronologicalTableau(concept.getNNF(),workingRule);
                    if(!flag.SAT()){
                        LoggerManager.writeDebug("ALL UNSATISFIABLE", oldChronologicalTableau.class);
                        workingRule =  dependency.get(workingRule);
                        backtrack();
                        check = false;
                    }
                    else
                        related.add(related.size(),flag);
                }
                i++;
            }

            if(check){
                someRelation.put(oe,related);
                workingRule++;
            }

        }

    }

    /**
     * Gets parent.
     *
     * @return the parent
     */
    public int getParent() {
        return parent;
    }

    /**
     * Gets abox.
     *
     * @return the abox
     */
    public List<OWLClassExpression> getAbox() {
        return Abox;
    }


    private void backtrack() {
        LoggerManager.writeDebug("BACKTRACK :" + workingRule, oldChronologicalTableau.class);

        iteration++;

        if(branchingNode.size()!=0) {

            oldNode workingNode = nodeList.get(workingRule);

            for (Integer i: nodeList.keySet()) {
                if(i>workingRule)
                    nodeList.remove(nodeList.get(i));

            }

            Abox.removeAll(Collections.unmodifiableList(Abox));
            Abox.addAll(workingNode.getAbox());
            dependency = dependency.subList(0,Abox.size());
            Set<OWLObjectPropertyExpression> listSome = someRelation.keySet();
            for (OWLObjectPropertyExpression oe : listSome) {

                ArrayList<oldChronologicalTableau> t = new ArrayList<>(someRelation.remove(oe));

                if(t!= null && t.size()!=0){

                    for (int i = t.size() - 1; i >=0 ; i--) {
                        if(t.get(i).getParent() > workingRule)
                            t.remove(i);
                    }

                    if(t.size()!=0)
                        someRelation.put(oe,t);
                }
            }
        }
        else
            workingRule = -1;

    }

    private boolean isWorking() {
        return !((workingRule>=Abox.size()) || (workingRule<0));

    }

    private void checkIntersection(List<OWLClassExpression> disjointedList){
        if(disjointedList!=null) {
            for (OWLClassExpression ce: disjointedList ) {
                if(!Abox.contains(ce)){
                    dependency.add(Abox.size(),workingRule);
                    Abox.add(Abox.size(),ce);
                }
            }
        }
    }


    private boolean checkClash() {

        for (int i = 0; i < Abox.size(); i++) {

            OWLClassExpression c = Abox.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < Abox.size(); i1++) {

                OWLClassExpression c1 = Abox.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    LoggerManager.writeDebug("CLASH "+ OntologyRenderer.render(c) + " " +OntologyRenderer.render(c1), oldChronologicalTableau.class);
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Get model string.
     *
     * @return the string
     */
    public String getModel(){
        String model = "";
        for (OWLClassExpression e: Abox) {
            if(e != null) {
                ClassExpressionType pe = e.getClassExpressionType();
                switch (pe) {
                    case OWL_CLASS:
                    case OBJECT_COMPLEMENT_OF:
                        model=model.concat(" " + OntologyRenderer.render((e)));
                        if(parent==-1) {
                            model = model.concat(" |");
                        }

                        break;
                }
            }
        }
        if(!someRelation.isEmpty()) {
            Set<OWLObjectPropertyExpression> key = someRelation.keySet();
            for (OWLObjectPropertyExpression oe : key) {
                if (oe != null) {
                    List<oldChronologicalTableau> related = someRelation.get(oe);
                    for (oldChronologicalTableau t : related) {
                        model=model.concat(" EXIST " + OntologyRenderer.render((oe)) + ". {");
                        model = model.concat(t.getModel());
                        if(model.chars().filter(ch -> ch == '}').count() < model.chars().filter(ch -> ch == '{').count())
                            model=model.concat(" }");
                    }
                }
            }
            if (parent==-1 && !model.endsWith("|")) {
                model = model.concat(" |");
            }
        }
        return model;
    }

    /**
     * Get iteration integer.
     *
     * @return the integer
     */
    public Integer getIteration(){

        int it=iteration;

        Set<OWLObjectPropertyExpression> listSome = someRelation.keySet();

        for (OWLObjectPropertyExpression oe : listSome) {

            ArrayList<oldChronologicalTableau> lt = new ArrayList<>(someRelation.get(oe));

            for (oldChronologicalTableau t: lt) {
                it+=t.getIteration();

            }
        }
        return it;
    }

}




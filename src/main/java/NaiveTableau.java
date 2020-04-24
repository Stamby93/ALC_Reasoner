import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;

public class NaiveTableau implements Tableau{

    private final List<OWLClassExpression> Abox;

    private final List<Integer> branchingNode;

    private List<Integer> dependency;

    private Map<Integer, Node> nodeList;

    private int workingRule = 0;

    private Map<OWLObjectPropertyExpression, List<NaiveTableau>> someRelation;

    private final int parent;

    private static String model;

    protected NaiveTableau(OWLClassExpression concept, int parent) {

        Abox = new ArrayList<>();
        Abox.add(0, concept);
        branchingNode = new ArrayList<>();
        someRelation = new HashMap<>();
        nodeList = new HashMap<>();
        dependency = new ArrayList<>();
        dependency.add(0,0);
        this.parent = parent;
        model = "";
    }





    public boolean checkSome(OWLClassExpression expression) {

        return Abox.contains(expression);

    }


    @Override
    public boolean SAT() {
        LoggerManager.writeDebug("SAT: "+ parent, NaiveTableau.class);
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
                    LoggerManager.writeDebug("CLASS :"+ OntologyRenderer.render(Abox.get(workingRule)), NaiveTableau.class);
                    if(checkClash()){
                        workingRule = dependency.get(workingRule);
                        backtrack();
                    }
                    else
                        workingRule++;
                    break;
            }

        }

        LoggerManager.writeDebug("SAT: "+ parent+ " " + (workingRule > 0), NaiveTableau.class);

        return workingRule >= 0;

    }

    private void applyIntersection(){
        LoggerManager.writeDebug("INTERSECTION "+ OntologyRenderer.render(Abox.get(workingRule)), NaiveTableau.class);
        Node workingNode = new Node(Abox, workingRule);
        List<OWLClassExpression> flag = workingNode.applyRule();
        checkIntersection(flag);
        nodeList.put(workingRule,workingNode);
        workingRule++;
    }

    private void applyUnion(){
        LoggerManager.writeDebug("Rule: " + workingRule + " UNION: " + OntologyRenderer.render(Abox.get(workingRule)), NaiveTableau.class);
        Node workingNode;

        if(branchingNode.size()!=0 && branchingNode.contains(Integer.valueOf(workingRule))) {
            workingNode = nodeList.get(workingRule);

        } else{
            workingNode = new Node(Abox, workingRule);
            branchingNode.add(branchingNode.size(),workingRule);

        }
        List<OWLClassExpression> choice = workingNode.applyRule();
        if(choice!=null && choice.get(0)!=null) {
            LoggerManager.writeDebug("CHOICE " + OntologyRenderer.render(choice.get(0)),NaiveTableau.class);
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
        LoggerManager.writeDebug("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(rule), NaiveTableau.class);

        NaiveTableau direct;
        OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();

        //VERIFICO SE INDIVIDUO HA LA RELAZIONE
        List<NaiveTableau> related = someRelation.get(oe);
        direct = new NaiveTableau(filler, workingRule);
        if (related == null) {

            if(direct.SAT()){
                someRelation.put(oe, Collections.singletonList(direct));
                workingRule++;
            }
            else{
                LoggerManager.writeDebug("SOME UNSATISFIABLE",NaiveTableau.class);

                workingRule = dependency.get(workingRule);

                backtrack();
            }
        }
        //CASO IN CUI RELAZIONE RICHIESTA ESISTE, VERIFICO SE E' PRESENTE LA REGOLA NEL RULE SET
        else{
            boolean check = false;

            for (NaiveTableau t : related) {

                if (t.checkSome(filler)) {
                    check = true;
                    break;
                }

            }
            if (!check) {
                //CASO IN CUI NESSUNO DEI NODI CON QUESTA RELAZIONE HA LA FORMULA TRA IL SUO RULE SET
                //QUINDI INSTANZIO NUOVO INDIVIDUO E MI SALVO LA RELAZIONE
                if(direct.SAT()) {

                    ArrayList<NaiveTableau> flag = new ArrayList<>(someRelation.get(oe));
                    flag.add(direct);
                    someRelation.put(oe, flag);
                    workingRule++;
                }
                else{
                    LoggerManager.writeDebug("SOME UNSATISFIABLE", NaiveTableau.class);
                    workingRule = dependency.get(workingRule);
                    backtrack();
                }
            } else{
                LoggerManager.writeDebug("SOME ALREADY PRESENT", NaiveTableau.class);
                workingRule++;
            }
        }
    }

    private void applyAll(OWLClassExpression rule){
        LoggerManager.writeDebug("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(rule),NaiveTableau.class);

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        if (someRelation.get(oe) == null){

            LoggerManager.writeDebug("ALL NO CONDIZIONI",NaiveTableau.class);
            workingRule++;
        }
        else{

            ArrayList<NaiveTableau> related = new ArrayList<>(someRelation.get(oe));
            int j = related.size();
            for (int i  = 0; (i < j) && check; i++){

                NaiveTableau t = related.get(i);
                if(!t.checkSome(filler)){

                    ArrayList<OWLClassExpression> operands = new ArrayList<>();
                    operands.add(t.getAbox().get(0));
                    operands.add(filler);
                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);

                    NaiveTableau flag = new NaiveTableau(concept.getNNF(),workingRule);
                    if(!flag.SAT()){
                        LoggerManager.writeDebug("ALL UNSATISFIABLE",NaiveTableau.class);
                        workingRule =  dependency.get(workingRule);
                        backtrack();
                        check = false;
                    }
                    else
                        related.add(related.size(),flag);
                }
                i++;
            }
            someRelation.put(oe,related);
            if(check)
                workingRule++;

        }

    }

    public int getParent() {
        return parent;
    }

    public List<OWLClassExpression> getAbox() {
        return Abox;
    }


    private void backtrack() {
        LoggerManager.writeDebug("BACKTRACK :" + workingRule,NaiveTableau.class);

        if(branchingNode.size()!=0) {
            Node workingNode = nodeList.get(workingRule);
            for (Integer i: nodeList.keySet()) {
                if(i>workingRule)
                    nodeList.remove(nodeList.get(i));

            }
            Abox.removeAll(Abox);
            Abox.addAll(workingNode.getAbox());
            dependency = dependency.subList(0,Abox.size());
            for (OWLObjectPropertyExpression oe : someRelation.keySet()) {
                List<NaiveTableau> t = someRelation.get(oe);
                if(t!= null && t.size()!=0){
                    for (int i = t.size() - 1 ; i >=0 ; i--) {
                        if(t.get(i).getParent() > workingRule)
                            t = t.subList(0, i);
                    }
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
                    LoggerManager.writeDebug("CLASH "+ OntologyRenderer.render(c) + " " +OntologyRenderer.render(c1), NaiveTableau.class);
                    return true;
                }
            }
        }
        return false;

    }

    public String getModel(){
        buildModel(false);
        return model;
    }

    private void buildModel(){
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
                    List<NaiveTableau> related = someRelation.get(oe);
                    model=model.concat(" EXIST " + OntologyRenderer.render((oe)) + ". {");
                    for (NaiveTableau t : related) {
                        t.buildModel();
                        if(model.chars().filter(ch -> ch == '}').count() < model.chars().filter(ch -> ch == '{').count())
                            model=model.concat(" }");
                    }
                }
            }
            if (parent==-1 && !model.endsWith("|")) {
                model = model.concat(" |");
            }
        }
    }
}




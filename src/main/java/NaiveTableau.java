import org.semanticweb.owlapi.model.*;

import java.util.*;

public class NaiveTableau implements Tableau{

    private final List<OWLClassExpression> Abox;

    private final List<Node> branchingNode;

    private List<Node> nodeList;

    private int workingRule;

    private final Map<OWLObjectPropertyExpression, List<NaiveTableau>> someRelation;

    private final int parent;

    private int modelLength;

    private static String model;

    protected NaiveTableau(OWLClassExpression concept, int parent) {

        Abox = new ArrayList<>();
        Abox.add(Abox.size(), concept);
        branchingNode = new ArrayList<>();
        someRelation = new HashMap<>();
        nodeList = new ArrayList<>();
        modelLength = 1;
        this.parent = parent;
        model = "";
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


    public boolean checkSome(OWLClassExpression expression) {

            return Abox.contains(expression);

    }


    public boolean checkAll(OWLClassExpression expression) {

        ArrayList<OWLClassExpression> flag = new ArrayList<>(Abox);
        checkIntersection(Collections.singletonList(expression));
        if(Abox.size() != flag.size()){
            nodeList.add(nodeList.size(), new Node(Abox, workingRule));
            boolean result = SAT();
            Abox.removeAll(Abox);
            Abox.addAll(flag);
            return result;
        }

        return true;
    }


    @Override
    public boolean SAT() {
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
                    if(checkClash()){
                        if(branchingNode.size()!=0){
                            workingRule-=modelLength;
                            backtrack();
                        } else
                            return false;
                    }
                    else
                        workingRule++;
                    break;
            }

        }
        return workingRule > 0;
    }

    private void applyIntersection(){
        LoggerManager.writeDebug("INTERSECTION "+ OntologyRenderer.render(Abox.get(workingRule)), NaiveTableau.class);
        Node workingNode = new Node(Abox, workingRule);
        checkIntersection(workingNode.applyRule());
        modelLength = Abox.size()-1;
        LoggerManager.writeDebug("MODEL LENGht"+ modelLength, NaiveTableau.class);
        nodeList.add(workingRule, workingNode);
        workingRule++;
    }

    private void applyUnion(){
        LoggerManager.writeDebug("UNION " + OntologyRenderer.render(Abox.get(workingRule)), NaiveTableau.class);
        Node workingNode;
        if(branchingNode.size()!=0 && branchingNode.get(branchingNode.size()-1).getWorkingRule()==workingRule) {
            workingNode = branchingNode.get(branchingNode.size()-1);
        } else{
            workingNode = new Node(Abox, workingRule);
            branchingNode.add(branchingNode.size(),workingNode);
        }
        List<OWLClassExpression> choice = workingNode.applyRule();
        if(choice.get(0)!=null) {
            LoggerManager.writeDebug("CHOICE " + OntologyRenderer.render(choice.get(0)),NaiveTableau.class);
            checkIntersection(choice);
            nodeList.add(workingRule, workingNode);
            if (!checkClash()){
                workingRule++;
            }
            else
                backtrack();
        }
        else{
            branchingNode.remove(workingNode);
            workingRule--;
            backtrack();
        }
    }

    private void applySome(OWLClassExpression rule){
        LoggerManager.writeDebug("SOME: "+ workingRule + " " + OntologyRenderer.render(rule), NaiveTableau.class);

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
                LoggerManager.writeDebug("SOME FALLITO",NaiveTableau.class);

                workingRule--;
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
                    LoggerManager.writeDebug("SOME FALLITO", NaiveTableau.class);
                    workingRule--;
                    backtrack();
                }
            } else
                workingRule++;
        }
    }

    private void applyAll(OWLClassExpression rule){
        LoggerManager.writeDebug("ALL "+ workingRule + " " + OntologyRenderer.render(rule),NaiveTableau.class);

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        List<NaiveTableau> related = someRelation.get(oe);
        if (related == null){

                workingRule++;
            //backtrack();
        }
        else{
            
            for (NaiveTableau t: related){

                if(!t.checkAll(filler)){
                    workingRule -=  modelLength;
                    LoggerManager.writeDebug("ALL FALLITO",NaiveTableau.class);
                    backtrack();
                    check = false;
                    break;
                }
            }

            if(check)
                workingRule++;

        }

    }

    public int getParent() {
        return parent;
    }

    private void backtrack() {
        LoggerManager.writeDebug("BACKTRACK :" + workingRule,NaiveTableau.class);

        if(branchingNode.size()!=0) {
            Node workingNode = nodeList.get(workingRule);
            nodeList = nodeList.subList(0, workingRule);
            Abox.removeAll(Abox);
            Abox.addAll(workingNode.getAbox());
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
                if(!Abox.contains(ce))
                    Abox.add(Abox.size(),ce);
            }
        }
    }

    public String getModel(){
        buildModel(false);
        return model;
    }

    private void buildModel(boolean exist){
        for (OWLClassExpression e: Abox) {
            if(e != null) {
                ClassExpressionType pe = e.getClassExpressionType();
                switch (pe) {
                    case OWL_CLASS:
                    case OBJECT_COMPLEMENT_OF:
                        model=model.concat(" " + OntologyRenderer.render((e))+" |");
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
                        t.buildModel(true);
                        model=model.concat(" }");
                    }
                }
            }
            if (!exist)
                model=model.concat(" |");
        }
    }
}




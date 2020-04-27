import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;

public class myJumpingTableau implements Tableau{

    private final List<OWLClassExpression> Abox;

    private final List<Integer> branchingNode;

    private List<Tableau> nodeList;

    private int workingRule = 0;

    private int workingNode = 0;

    private final Map<OWLObjectPropertyExpression, List<Integer>> someRelation;

    private final Map<OWLObjectPropertyExpression, List<Integer>> allRelation;

    private final int parent;

    private int iteration=0;

    private List<Integer> clashList;

    private List<List<Integer>> dependency;



    protected myJumpingTableau(OWLClassExpression concept, int parent) {

        Abox = new ArrayList<>();
        Abox.add(0, concept);
        branchingNode = new ArrayList<>();
        someRelation = new HashMap<>();
        allRelation = new HashMap<>();
        nodeList = new ArrayList<>();
        dependency = new ArrayList<>();
        dependency.add(0,Collections.singletonList(-1));
        clashList = new ArrayList<>();
        this.parent = parent;
    }

    @Override
    public boolean SAT() {

        LoggerManager.writeDebug("SAT: "+ parent, myJumpingTableau.class);

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

                    LoggerManager.writeDebug("CLASS :"+ OntologyRenderer.render(Abox.get(workingRule)), myJumpingTableau.class);
                    if(checkClash()){
                        //workingNode--;
                        backtrack();
                    }
                    else
                        workingRule++;
                    break;
            }
            iteration++;
        }

        LoggerManager.writeDebug("SAT: "+ parent+ " " + ((workingRule >= 0) & (workingNode >= 0)), myJumpingTableau.class);

        if (parent==-1 && ((workingRule >= 0) & (workingNode >= 0))){
            LoggerManager.writeDebug("NUMERO ITERAZIONI: " + getIteration(), JumpingTableau.class);
            LoggerManager.writeDebug("MODELLO: " + getModel(), JumpingTableau.class);

        }


        return ((workingRule >= 0) & (workingNode >= 0));

    }

    public void addDependecy(int oldD, int newD){

        for(int i = oldD; i<newD; i++)
            dependency.add(i,branchingNode);

    }

    private void applyIntersection(){
        LoggerManager.writeDebug("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(Abox.get(workingRule)), myJumpingTableau.class);

        Tableau Node = new myNode(Abox, workingRule);
        Node.SAT();
        int old_dimension = Abox.size();
        int new_dimension = Node.getAbox().size();
        addDependecy(old_dimension,new_dimension);
        nodeList.add(workingNode,Node);
        Abox.removeAll(Abox);
        Abox.addAll(Node.getAbox());
        Collections.reverse(Abox);
        workingRule ++;
        workingNode ++;
    }

    private void applyUnion(){
        LoggerManager.writeDebug("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(Abox.get(workingRule)), myJumpingTableau.class);

        myNode Node;

        if(branchingNode.contains(Integer.valueOf(workingNode)))
            Node = (myNode)nodeList.get(workingNode);
        else{
            Node = new myNode(Abox, workingRule);
            branchingNode.add(branchingNode.size(),workingNode);
            nodeList.add(workingNode,Node);
        }

        boolean last = false;
        while(Node.SAT()){
            last = true;
            int old_dimension = Abox.size();
            ArrayList<OWLClassExpression> saveT = new ArrayList<>(Abox);
            Abox.removeAll(Abox);
            Abox.addAll(Node.getAbox());
            int new_dimension = Abox.size();

            LoggerManager.writeDebug("CHOICE " + OntologyRenderer.render(Abox.get(Abox.size()-1)), ChronologicalTableau.class);
            if(!Node.hasChoice()){
                branchingNode.remove(Integer.valueOf(workingNode));}

            addDependecy(old_dimension,new_dimension);

            if(checkClash()){
                Abox.removeAll(Abox);
                Abox.addAll(saveT);
                Collections.reverse(Abox);
                dependency = dependency.subList(0,old_dimension);
                iteration++;
                last = false;
            }
            else{

                workingRule++;
                workingNode++;
                break;
            }
        }

        if(!last)
            backtrack();


    }

    private void applySome(OWLClassExpression rule){
        LoggerManager.writeDebug("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(rule), myJumpingTableau.class);

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

                if (direct.getAbox().contains(filler)) {

                    LoggerManager.writeDebug("SOME ALREADY PRESENT", myJumpingTableau.class);
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
                    operands.add(direct.getAbox().get(0));

                }

                operands.add(filler);
                OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);
                filler = concept;

            }

            direct = new myJumpingTableau(filler, workingRule);
            nodeList.add(workingNode,direct);
            if(direct.SAT()) {

                related.add(related.size(),workingNode);
                someRelation.put(oe, related);
                workingNode++;
                workingRule++;
            }
            else{

                LoggerManager.writeDebug("SOME UNSATISFIABLE", myJumpingTableau.class);
                clashList = new ArrayList<>(dependency.get(workingRule));
                backtrack();

            }
        }
    }

    private void applyAll(OWLClassExpression rule){
        LoggerManager.writeDebug("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(rule), myJumpingTableau.class);

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        if (someRelation.get(oe) == null){
            LoggerManager.writeDebug("ALL NO CONDITIONS", myJumpingTableau.class);

            Tableau t = new myJumpingTableau(filler.getNNF(),workingRule);
            nodeList.add(workingNode,t);

            if(t.SAT()){

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
                clashList = new ArrayList<>(dependency.get(workingRule));
                backtrack();
            }
        }
        else{

            ArrayList<Integer> newRelated = new ArrayList<>();
            ArrayList<Integer> related = new ArrayList<>(someRelation.get(oe));

            if(allRelation.get(oe)!=null){

                related.addAll(new ArrayList<>(allRelation.get(oe)));
                Collections.sort(related);

            }

            for (int i  = 0 ; (i < related.size()) && check; i++){

                Tableau t = nodeList.get(related.get(i));

                if(!t.getAbox().contains(filler)){

                    ArrayList<OWLClassExpression> operands = new ArrayList<>();
                    operands.add(t.getAbox().get(0));
                    operands.add(filler);
                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);
                    myJumpingTableau flag = new myJumpingTableau(concept.getNNF(),workingRule);
                    nodeList.add(workingNode,t);

                    if(!flag.SAT()){

                        LoggerManager.writeDebug("ALL UNSATISFIABLE", myJumpingTableau.class);
                        backtrack();
                        check = false;

                    }
                    else{

                        newRelated.add(newRelated.size(),workingNode);
                        workingNode++;

                    }
                }
                i++;
            }

            if(check){

                if(newRelated.size()!=0){

                    Collections.sort(newRelated);

                    if(allRelation.get(oe)!=null)
                        newRelated.addAll(allRelation.get(oe));

                    allRelation.put(oe,newRelated);
                }

                workingRule++;

            }
        }
    }

    private void backtrack() {

        iteration++;

        if(clashList.size()!=0 && clashList.get(clashList.size()-1)!=-1) {

            workingNode = clashList.get(clashList.size()-1);
            nodeList = nodeList.subList(0,workingNode+1);
            cleanRelation(someRelation);
            cleanRelation(allRelation);

            Tableau Node = nodeList.get(workingNode);
            int dim = Node.getAbox().size();

            Abox.removeAll(Abox);
            Abox.addAll(Node.getAbox().subList(0,dim-2));
            workingRule = Node.getParent();
            dependency = dependency.subList(0,Abox.size());
        }
        else
            workingNode = -1;

        LoggerManager.writeDebug("BACKTRACK :" + workingNode, myJumpingTableau.class);

    }

    private boolean isWorking() {
        return !(((workingRule>=Abox.size()) || (workingRule<0)) || (workingNode<0));

    }

    private void cleanRelation(Map<OWLObjectPropertyExpression, List<Integer>> relation){
        Set<OWLObjectPropertyExpression> list = relation.keySet();
        for (OWLObjectPropertyExpression oe : list) {

            ArrayList<Integer> t = new ArrayList<>(relation.remove(oe));

            if(t!= null && t.size()!=0){

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

        for (int i = 0; i < Abox.size(); i++) {

            OWLClassExpression c = Abox.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < Abox.size(); i1++) {

                OWLClassExpression c1 = Abox.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    LoggerManager.writeDebug("CLASH "+ OntologyRenderer.render(c) + " " +OntologyRenderer.render(c1), JumpingTableau.class);
                    clashList = new ArrayList<>(dependency.get(i));
                    for (Integer j:dependency.get(i1)) {
                        if(!clashList.contains(j)){
                            clashList.add(j);
                        }

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
                    List<Integer> related = someRelation.get(oe);

                    for (Integer j : related) {
                        myJumpingTableau t = (myJumpingTableau)nodeList.get(j);
                        model=model.concat(" EXIST " + OntologyRenderer.render((oe)) + ". {");
                        model = model.concat(t.getModel());
                        if(model.chars().filter(ch -> ch == '}').count() < model.chars().filter(ch -> ch == '{').count())
                            model=model.concat(" }");
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
                        myJumpingTableau t = (myJumpingTableau)nodeList.get(j);
                        model=model.concat(" EXIST " + OntologyRenderer.render((oe)) + ". {");
                        model = model.concat(t.getModel());
                        if(model.chars().filter(ch -> ch == '}').count() < model.chars().filter(ch -> ch == '{').count())
                            model=model.concat(" }");
                    }
                }
            }
        }
        if (parent==-1 && !model.endsWith("|"))
            model = model.concat(" |");

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
    public List<OWLClassExpression> getAbox() {
        return Abox;
    }
}




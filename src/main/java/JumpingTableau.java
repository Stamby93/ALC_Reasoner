import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;

public class JumpingTableau implements Tableau{

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



    protected JumpingTableau(OWLClassExpression concept, int parent) {

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

        LoggerManager.writeDebugLog("SAT: "+ parent, JumpingTableau.class);

        while(isWorking()){

            //LoggerManager.writeDebugLog("DEPENDENCY: " + dependency, JumpingTableau.class);
            //LoggerManager.writeDebugLog("ABOX: "+ printAbox(), JumpingTableau.class);


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

                    LoggerManager.writeDebugLog("CLASS :"+ OntologyRenderer.render(Abox.get(workingRule)), JumpingTableau.class);
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

        LoggerManager.writeDebugLog("SAT: "+ parent+ " " + ((workingRule >= 0) & (workingNode >= 0)), JumpingTableau.class);

        if (parent==-1 && ((workingRule >= 0) & (workingNode >= 0))){
            LoggerManager.writeDebugLog("NUMERO ITERAZIONI: " + getIteration(), JumpingTableau.class);
            LoggerManager.writeDebugLog("MODELLO: " + getModel(), JumpingTableau.class);

        }


        return ((workingRule >= 0) & (workingNode >= 0));

    }

    public void addDependecy(int oldD, int newD, List<Integer> dep){

        for(int i = oldD; i<newD; i++)
            dependency.add(i,dep);

    }

    private String printAbox(){

        String a = new String("");
        int i = 0;
        for (OWLClassExpression e:Abox
             ) {
            a = a.concat("\n ELEMENT " + i + "):  " + OntologyRenderer.render(e));
            i++;
        }
        return a;
    }

    private void applyIntersection(){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(Abox.get(workingRule)), JumpingTableau.class);
        //LoggerManager.writeDebugLog("Dependency: " + dependency.get(workingRule), JumpingTableau.class);

        Tableau Node = new Node(Abox, workingRule);
        Node.SAT();
        int old_dimension = Abox.size();
        int new_dimension = Node.getConceptList().size();
        addDependecy(old_dimension,new_dimension,dependency.get(workingRule));
        nodeList.add(workingNode,Node);
        Abox.removeAll(Collections.unmodifiableList(Abox));
        Abox.addAll(Node.getConceptList());



        workingRule ++;
        workingNode ++;
    }

    private void applyUnion(){
        LoggerManager.writeDebugLog("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(Abox.get(workingRule)), JumpingTableau.class);
        //LoggerManager.writeDebugLog("Dependency: " + dependency.get(workingRule), JumpingTableau.class);


        Node Node;
        List<Integer> dep = new ArrayList<>();
        dep.add(Integer.valueOf(workingNode));


        if(branchingNode.contains(Integer.valueOf(workingNode))){
            Node = (Node)nodeList.get(workingNode);

        }
        else{
            Node = new Node(Abox, workingRule);
            branchingNode.add(branchingNode.size(),workingNode);
            nodeList.add(workingNode,Node);
        }

        boolean last = false;
        while(Node.SAT()){
            //LoggerManager.writeDebugLog("CLASH LIST " + clashList, JumpingTableau.class);

            last = true;
            int old_dimension = Abox.size();
            ArrayList<OWLClassExpression> saveT = new ArrayList<>(Abox);
            Abox.removeAll(Collections.unmodifiableList(Abox));
            Abox.addAll(Node.getConceptList());
            int new_dimension = Abox.size();

            LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(Abox.get(Abox.size()-1)), JumpingTableau.class);
            if(!Node.hasChoice()){
                branchingNode.remove(Integer.valueOf(workingNode));
                dep.remove(Integer.valueOf(workingNode));
                clashList.remove(Integer.valueOf(workingNode));
                dep.removeAll(dep);
                if(!clashList.isEmpty())
                    dep.addAll(clashList);
                else
                    dep.addAll(dependency.get(workingRule));
            }

            addDependecy(old_dimension,new_dimension, dep);

            if(checkClash()){

                if(clashList.contains(Integer.valueOf(workingNode))){
                    Abox.removeAll(Collections.unmodifiableList(Abox));
                    Abox.addAll(saveT);
                    dependency = dependency.subList(0,old_dimension);
                    if(clashList.size()>1)
                        dep.add(dep.size(),clashList.get(clashList.size()-2));

                }
                else{
                    last = false;
                    break;
                }
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
        LoggerManager.writeDebugLog("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(rule), JumpingTableau.class);
        //LoggerManager.writeDebugLog("Dependency: " + dependency.get(workingRule), JumpingTableau.class);


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

                    LoggerManager.writeDebugLog("SOME ALREADY PRESENT", JumpingTableau.class);
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

            ArrayList<Integer> allRelated = new ArrayList<>();
            clashList = new ArrayList<>(dependency.get(workingRule));
            List<Integer> tD;

            if(allRelation.get(oe)!=null){

                allRelated.addAll(allRelation.get(oe));
                ArrayList<OWLClassExpression> operands = new ArrayList<>();

                for (Integer i: allRelated) {

                    direct = nodeList.get(i);
                    operands.add(direct.getConceptList().get(0));

                    tD = dependency.get(direct.getParent());

                    for(int d = 0; d < tD.size(); d++){
                        if(!clashList.contains(tD.get(d)))
                            clashList.add(tD.get(d));
                    }

                }

                operands.add(filler);
                filler = new OWLObjectIntersectionOfImpl(operands);

            }

            direct = new JumpingTableau(filler, workingRule);
            nodeList.add(workingNode,direct);
            if(direct.SAT()) {

                related.add(related.size(),workingNode);
                someRelation.put(oe, related);
                workingNode++;
                workingRule++;
            }
            else{
                LoggerManager.writeDebugLog("SOME UNSATISFIABLE", JumpingTableau.class);

                Collections.sort(clashList);
                backtrack();

            }
        }
    }

    private void applyAll(OWLClassExpression rule){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(rule), JumpingTableau.class);
        //LoggerManager.writeDebugLog("Dependency: " + dependency.get(workingRule), JumpingTableau.class);


        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        if (someRelation.get(oe) == null){
            LoggerManager.writeDebugLog("ALL NO CONDITIONS", JumpingTableau.class);

            Tableau t = new JumpingTableau(filler.getNNF(),workingRule);
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
            clashList = new ArrayList<>(dependency.get(workingRule));

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
                    JumpingTableau flag = new JumpingTableau(concept.getNNF(),workingRule);
                    nodeList.add(workingNode,flag);

                    if(!flag.SAT()){

                        LoggerManager.writeDebugLog("ALL UNSATISFIABLE", JumpingTableau.class);
                        for (Integer j: dependency.get(t.getParent())) {
                            if(!clashList.contains(j))
                                clashList.add(j);
                        }
                        Collections.sort(clashList);
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

            workingNode = clashList.remove(clashList.size()-1);
            nodeList = nodeList.subList(0,workingNode+1);
            cleanRelation(someRelation);
            cleanRelation(allRelation);

            Tableau Node = nodeList.get(workingNode);
            int dim = Node.getConceptList().size();

            Abox.removeAll(Collections.unmodifiableList(Abox));
            Abox.addAll(Node.getConceptList().subList(0,dim-1));
            workingRule = Node.getParent();
            dependency = dependency.subList(0,Abox.size());
            int i = branchingNode.size() - 1;
            for (; i >= 0; i--) {
                if (branchingNode.get(i)>workingNode)
                    branchingNode.remove(i);

            }
        }
        else
            workingNode = -1;

        LoggerManager.writeDebugLog("BACKTRACK :" + workingNode, JumpingTableau.class);

    }

    private boolean isWorking() {
        return !(((workingRule>=Abox.size()) || (workingRule<0)) || (workingNode<0));

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

        for (int i = 0; i < Abox.size(); i++) {

            OWLClassExpression c = Abox.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < Abox.size(); i1++) {

                OWLClassExpression c1 = Abox.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    LoggerManager.writeDebugLog("CLASH "+ OntologyRenderer.render(c) + " " +OntologyRenderer.render(c1), JumpingTableau.class);
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
        String model = "| ";
        for (OWLClassExpression e: Abox) {
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
        return Abox;
    }
}




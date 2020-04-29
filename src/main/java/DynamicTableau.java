import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;

public class DynamicTableau implements Tableau{

    private List<OWLClassExpression> Abox;

    private List<Integer> branchingNode;

    private List<Tableau> nodeList;

    private int workingRule = 0;
    
    private int temporanyNode = 0;

    private int workingNode = 0;

    private final Map<OWLObjectPropertyExpression, List<Integer>> someRelation;

    private final Map<OWLObjectPropertyExpression, List<Integer>> allRelation;

    private final int parent;

    private int iteration=0;

    private List<Integer> clashList;

    private List<List<Integer>> dependency;

    //private List<List<Integer>> INVdependency;



    protected DynamicTableau(OWLClassExpression concept, int parent) {

        Abox = new ArrayList<>();
        Abox.add(0, concept);
        branchingNode = new ArrayList<>();
        someRelation = new HashMap<>();
        allRelation = new HashMap<>();
        nodeList = new ArrayList<>();
        dependency = new ArrayList<>();
        clashList = new ArrayList<>();
        //INVdependency = new ArrayList<>();
        dependency.add(0,Collections.singletonList(-1));
        this.parent = parent;
    }

    @Override
    public boolean SAT() {

        LoggerManager.writeDebugLog("SAT: "+ parent, DynamicTableau.class);

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

                    LoggerManager.writeDebugLog("CLASS :"+ OntologyRenderer.render(Abox.get(workingRule)), DynamicTableau.class);
                    if(checkClash())
                        backtrack();
                    else
                        workingRule++;
                    break;
            }
            iteration++;
        }

        LoggerManager.writeDebugLog("SAT: "+ parent+ " " + ((workingRule >= 0) & (workingNode >= 0)), DynamicTableau.class);

        if (parent==-1 && ((workingRule >= 0) & (workingNode >= 0))){
            LoggerManager.writeDebugLog("NUMERO ITERAZIONI: " + getIteration(), DynamicTableau.class);
            LoggerManager.writeDebugLog("MODELLO: " + getModel(), DynamicTableau.class);

        }


        return ((workingRule >= 0) & (workingNode >= 0));

    }

    private void addDependecy(int oldD, int newD, List<Integer> dep){

        for(int i = oldD; i<newD; i++)
            dependency.add(i,dep);

    }

    private void updateDependecy(int start){
        

        for(int i = start; i<dependency.size(); i++)
            if(dependency.get(i).contains(workingNode))
                dependency.set(i,branchingNode);

    }



    private void applyIntersection(){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(Abox.get(workingRule)), DynamicTableau.class);

        Tableau Node = new subNode(Abox, workingRule);
        Node.SAT();
        int old_dimension = Abox.size();
        int new_dimension = Node.getAbox().size();
        addDependecy(old_dimension,new_dimension,dependency.get(workingRule));
        nodeList.add(workingNode,Node);
        Abox.removeAll(Abox);
        Abox.addAll(Node.getAbox());
        workingRule ++;
        workingNode ++;
    }

    private void applyUnion(){
        LoggerManager.writeDebugLog("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(Abox.get(workingRule)), DynamicTableau.class);

        subNode Node;

        if(branchingNode.contains(Integer.valueOf(workingNode)))
            Node = (subNode)nodeList.get(workingNode);
        else{
            Node = new subNode(Abox, workingRule);
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

            LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(Abox.get(Abox.size()-1)), ChronologicalTableau.class);

            if(!Node.hasChoice()){
                branchingNode.remove(Integer.valueOf(workingNode));
                addDependecy(old_dimension,new_dimension, branchingNode);
            }
            else
                addDependecy(old_dimension,new_dimension, Collections.singletonList(workingRule));

            if(checkClash()){
                Abox.removeAll(Abox);
                Abox.addAll(saveT);
                //Collections.reverse(Abox);
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
        LoggerManager.writeDebugLog("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(rule), DynamicTableau.class);

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

                    LoggerManager.writeDebugLog("SOME ALREADY PRESENT", DynamicTableau.class);
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
                    operands.add(direct.getAbox().get(0));

                    tD = dependency.get(direct.getParent());

                    for(int d = 0; d<tD.size(); d++){
                        if(!clashList.contains(tD.get(d)))
                            clashList.add(tD.get(d));
                    }
                }

                operands.add(filler);
                filler = new OWLObjectIntersectionOfImpl(operands);

            }

            direct = new DynamicTableau(filler, workingRule);
            nodeList.add(workingNode,direct);
            if(direct.SAT()) {

                related.add(related.size(),workingNode);
                someRelation.put(oe, related);
                workingNode++;
                workingRule++;
            }
            else{
                LoggerManager.writeDebugLog("SOME UNSATISFIABLE", DynamicTableau.class);

                Collections.sort(clashList);
                backtrack();

            }
        }
    }

    private void applyAll(OWLClassExpression rule){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(rule), DynamicTableau.class);

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        if (someRelation.get(oe) == null){
            LoggerManager.writeDebugLog("ALL NO CONDITIONS", DynamicTableau.class);

            Tableau t = new DynamicTableau(filler.getNNF(),workingRule);
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
                    DynamicTableau flag = new DynamicTableau(concept.getNNF(),workingRule);
                    nodeList.add(workingNode,t);

                    if(!flag.SAT()){
                        LoggerManager.writeDebugLog("ALL UNSATISFIABLE", DynamicTableau.class);

                        clashList = new ArrayList<>(dependency.get(workingRule));
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

    private List<Integer> cleanAbox(int start, Integer dep){

        ArrayList<List<Integer>> newDep = new ArrayList<>(dependency.subList(0,start));
        ArrayList<OWLClassExpression> newAbox = new ArrayList<>(Abox.subList(0,start));
        List<Integer> flag;
        List<Integer> toDelete = new ArrayList<>();
        int shift = 0;
        
        for(int i = start; i < dependency.size(); i++){

            flag = dependency.get(i);

            if(!flag.contains(dep)){
                newAbox.add(newAbox.size(), Abox.get(i));
                newDep.add(newDep.size(), dependency.get(i));
            }
            else{
                if(i<workingRule)
                    shift++;
                toDelete.add(Integer.valueOf(i));

            }
        }
        workingRule-=shift;
        Abox.removeAll(Abox);
        Abox.addAll(newAbox);
        dependency.removeAll(dependency);
        dependency.addAll(newDep);
        Collections.sort(toDelete);
        return toDelete;

    }

    private void backtrack() {

        iteration++;

        if(clashList.size()!=0 && clashList.get(clashList.size()-1)!=-1) {

            nodeList.remove(workingNode);

            workingNode = clashList.remove(clashList.size()-1);
            
            subNode t = (subNode)nodeList.get(workingNode);

            //LoggerManager.writeDebugLog("OLD DIMENSION :"+ workingNode+ "  " + Abox.size(), DynamicTableau.class);


            List<Integer> ruleToDelete = cleanAbox(t.getParent(),workingNode);

            //LoggerManager.writeDebugLog("NEW DIMENSION :"+ toDelete+ "  " + Abox.size(), DynamicTableau.class);

            List<Integer> nodeToDelete = new ArrayList<>();


            nodeToDelete.addAll(cleanNodeList(ruleToDelete));
            nodeToDelete.addAll(cleanRelation(someRelation,ruleToDelete));
            nodeToDelete.addAll(cleanRelation(allRelation,ruleToDelete));

            Collections.sort(nodeToDelete);

            //cleanNodeList(toDelete);

            Integer old_choice = ruleToDelete.remove(0);
            int shift = 0;
            for (Integer k: ruleToDelete) {
                if(k < old_choice)
                    shift++;

            }

            List<OWLClassExpression> nAbox;
            ArrayList<OWLClassExpression> oldAbox = new ArrayList<>(Abox);
            OWLClassExpression newrule;

            boolean goOn = false;
            Abox.removeAll(Abox);
            Abox.addAll(oldAbox.subList(0,old_choice));
            while(t.hasChoice()){
                t.SAT();
                nAbox = t.getAbox();
                newrule = nAbox.get(nAbox.size()-1);

                Abox.add(Abox.size(),newrule);
                Abox.addAll(Abox.size(), oldAbox.subList(old_choice,oldAbox.size()));
                if(checkClash()){
                    Abox.removeAll(Abox);
                    Abox.addAll(oldAbox);
                }
                else {
                    
                    goOn = true;
                    
                    for (int i = workingNode + 1; i < nodeList.size(); i++) {
                        if(branchingNode.contains(Integer.valueOf(i))){
                            t = (subNode) nodeList.get(i);
                            t.updateStatus(ruleToDelete, old_choice, newrule);
                        }
                    }
                    
                    workingNode = temporanyNode;
                    break;
                }
            }
            
            if(!t.hasChoice()){
                branchingNode.remove(Integer.valueOf(workingNode));
                updateDependecy(t.getParent());
            }
            if (!goOn)
                backtrack();
        }
        else
            workingNode = -1;

        LoggerManager.writeDebugLog("BACKTRACK :" + workingNode, DynamicTableau.class);

    }

    private List<Integer> cleanNodeList(List<Integer> toDelete) {

        List<Integer> nodeToDelete = new ArrayList<>();
        Tableau t;

        for(int i = 0; i< nodeList.size(); i++){

            t = nodeList.get(i);
            if(toDelete.contains(t.getParent()))
                nodeToDelete.add(Integer.valueOf(i));

        }

        return nodeToDelete;

    }


    private boolean isWorking() {
        return !(((workingRule>=Abox.size()) || (workingRule<0)) || (workingNode<0));

    }

    private List<Integer> cleanRelation(Map<OWLObjectPropertyExpression, List<Integer>> relation, List<Integer> toDelete){
        Set<OWLObjectPropertyExpression> list = relation.keySet();
        Tableau flag;
        List<Integer> nodeToDelete = new ArrayList<>();
        
        for (OWLObjectPropertyExpression oe : list) {

            ArrayList<Integer> t = new ArrayList<>(relation.remove(oe));

            if(t.size()!=0){

                for (int i = t.size() - 1; i >=0 ; i--) {

                    flag = nodeList.get(i);
                    if(toDelete.contains(flag.getParent())){
                        t.remove(i);
                        nodeToDelete.add(Integer.valueOf(i));
                    }
                }

                if(t.size()!=0)
                    relation.put(oe,t);
            }
        }

        return nodeToDelete;
    }

    private boolean checkClash() {

        for (int i = 0; i < Abox.size(); i++) {

            OWLClassExpression c = Abox.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < Abox.size(); i1++) {

                OWLClassExpression c1 = Abox.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    LoggerManager.writeDebugLog("CLASH "+ OntologyRenderer.render(c) + " " +OntologyRenderer.render(c1), DynamicTableau.class);
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
    public List<OWLClassExpression> getAbox() {
        return Abox;
    }
}




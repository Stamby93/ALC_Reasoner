import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;

public class DynamicTableau implements Tableau{

    private List<OWLClassExpression> Abox;

    private List<List<Integer>> dependency;

    private final List<Integer> actualDependency;

    private List<Integer> clashList;

    private final Map<Integer, subNode> nodeList;

    private int workingRule = 0;

    private final HashMap<OWLObjectPropertyExpression, List<DynamicTableau>> someRelation;

    private final int parent;

    private int iteration = 0;
    private int temporanyRule;

    protected DynamicTableau(OWLClassExpression concept, int parent) {

        Abox = new ArrayList<>();
        Abox.add(0, concept);
        someRelation = new HashMap<>();
        nodeList = new HashMap<>();
        dependency = new ArrayList<>();
        dependency.add(0,Collections.singletonList(-1));
        actualDependency = new ArrayList<>();
        this.parent = parent;
    }





    public boolean checkSome(OWLClassExpression expression) {

        return Abox.contains(expression);

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
                    if(checkClash()){

                        if(clashList!= null && clashList.size()!=0) {
                            temporanyRule = workingRule;
                            workingRule = clashList.get(clashList.size()-1);
                            backtrack();
                        }
                        else
                            workingRule = -1;
                    }
                    else
                        workingRule++;
                    break;
            }
            iteration++;

        }

        LoggerManager.writeDebugLog("SAT: "+ parent+ " " + (workingRule > 0), DynamicTableau.class);
        if (parent==-1){
            LoggerManager.writeDebugLog("NUMERO ITERAZIONI: " + getIteration(), oldJumpingTableau.class);
            LoggerManager.writeDebugLog("MODELLO: " + getModel(), oldJumpingTableau.class);
        }

        return workingRule >= 0;

    }

    private void applyIntersection(){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(Abox.get(workingRule)), DynamicTableau.class);
        subNode workingNode = new subNode(new ArrayList<>(Abox), workingRule);
        List<OWLClassExpression> flag = workingNode.applyRule();
        checkIntersection(flag);
        nodeList.put(workingRule,workingNode);
        workingRule++;
    }

    private void applyUnion(){
        LoggerManager.writeDebugLog("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(Abox.get(workingRule)), DynamicTableau.class);
        subOldNode workingNode;

        if(actualDependency.size()!=0 && actualDependency.contains(workingRule))
            workingNode = nodeList.get(workingRule);
        else
            workingNode = new subOldNode(new ArrayList<>(Abox), workingRule);

        List<OWLClassExpression> choice = workingNode.applyRule();
        if(choice!=null && choice.get(0)!=null) {
            LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(choice.get(0)),DynamicTableau.class);
            if(workingNode.hasChoice() && !actualDependency.contains(workingRule))
                actualDependency.add(actualDependency.size(),workingRule);
            else
                actualDependency.remove(Integer.valueOf(workingRule));

            checkIntersection(choice);
            nodeList.put(workingRule, workingNode);
            if (!checkClash()){
                workingRule++;
            }
            else{
                temporanyRule = workingRule;
                workingRule = clashList.get(clashList.size()-1);
                backtrack();
            }
        }
    }

    private void applySome(OWLClassExpression rule){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(rule), DynamicTableau.class);

        DynamicTableau direct;
        OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();

        //VERIFICO SE INDIVIDUO HA LA RELAZIONE
        List<DynamicTableau> related = someRelation.get(oe);
        direct = new DynamicTableau(filler, workingRule);

        if (related != null && related.size()!=0) {
            //CASO IN CUI RELAZIONE RICHIESTA ESISTE, VERIFICO SE E' PRESENTE LA REGOLA NEL RULE SET
            boolean check = false;

            for (DynamicTableau t : related) {

                if (t.checkSome(filler)) {

                    check = true;

                    break;
                }

            }
            if (!check) {
                //CASO IN CUI NESSUNO DEI NODI CON QUESTA RELAZIONE HA LA FORMULA TRA IL SUO RULE SET
                //QUINDI INSTANZIO NUOVO INDIVIDUO E MI SALVO LA RELAZIONE
                if(direct.SAT()) {

                    ArrayList<DynamicTableau> flag = new ArrayList<>(someRelation.get(oe));
                    flag.add(direct);
                    someRelation.put(oe, flag);
                    workingRule++;
                }
                else{
                    LoggerManager.writeDebugLog("SOME UNSATISFIABLE", DynamicTableau.class);
                    temporanyRule = workingRule;
                    List<Integer> dep = dependency.get(workingRule);
                    workingRule = dep.get(dep.size()-1);
                    backtrack();
                }
            } else{
                LoggerManager.writeDebugLog("SOME ALREADY PRESENT", DynamicTableau.class);
                workingRule++;
            }


        }
        else{

            if(direct.SAT()){
                someRelation.put(oe, Collections.singletonList(direct));
                workingRule++;
            }
            else{
                LoggerManager.writeDebugLog("SOME UNSATISFIABLE",DynamicTableau.class);

                List<Integer> dep = dependency.get(workingRule);
                if(dep.size()!=0) {
                    temporanyRule = workingRule;
                    workingRule = dep.get(dep.size()-1);
                    backtrack();
                }
                else
                    workingRule = -1;
            }
        }
    }

    private void applyAll(OWLClassExpression rule){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(rule),DynamicTableau.class);

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        if (someRelation.get(oe) == null){

            LoggerManager.writeDebugLog("ALL NO CONDITIONS",DynamicTableau.class);
            workingRule++;
        }
        else{

            ArrayList<DynamicTableau> related = new ArrayList<>(someRelation.get(oe));
            int j = related.size();
            for (int i  = 0; (i < j) && check; i++){

                DynamicTableau t = related.get(i);
                if(!t.checkSome(filler)){

                    ArrayList<OWLClassExpression> operands = new ArrayList<>();
                    operands.add(t.getConceptList().get(0));
                    operands.add(filler);
                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);

                    DynamicTableau flag = new DynamicTableau(concept.getNNF(),workingRule);
                    if(!flag.SAT()){
                        LoggerManager.writeDebugLog("ALL UNSATISFIABLE",DynamicTableau.class);
                        clashList = new ArrayList<>(dependency.get(workingRule));
                        for (Integer k :dependency.get(t.getParent())) {
                            if(!clashList.contains(k)){
                                clashList.add(k);
                            }

                        }
                        Collections.sort(clashList);

                        if(clashList.size()!=0){
                            temporanyRule = workingRule;
                            workingRule =  clashList.get(clashList.size()-1);
                            backtrack();
                        }
                        else
                            workingRule = -1;

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

    public int getParent() {
        return parent;
    }

    public List<OWLClassExpression> getAbox() {
        return Abox;
    }


    private void backtrack() {
        LoggerManager.writeDebugLog("BACKTRACK :" + workingRule,DynamicTableau.class);

        iteration++;

        if(actualDependency.size()!=0) {

            subOldNode workingNode = nodeList.get(workingRule);

            if(workingNode.hasChoice()){

                List<OWLClassExpression> choice = workingNode.applyRule();
                LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(choice.get(0)),DynamicTableau.class);
                Abox.set(workingNode.getAbox().size()-1,choice.get(0));
                if(!checkClash()){
                    for (int i = workingRule; i< nodeList.size(); i++){
                        nodeList.get(i).alterRule(choice.get(0),workingNode.getAbox().size()-1);
                    }
                    if(!workingNode.hasChoice()){
                        actualDependency.remove(Integer.valueOf(workingRule));
                        for(int i = 0; i<dependency.size(); i++){
                            if(dependency.get(i).contains(Integer.valueOf(workingRule))) {
                                dependency.get(i).remove(Integer.valueOf(workingRule));
                            }
                        }
                    }
                    if(workingRule==temporanyRule)
                        workingRule++;
                    else
                        workingRule = temporanyRule;
                }
                else{
System.out.println("CLASHHHHHH");
                    if(clashList.size()!=0){
                        //clashList.remove(Integer.valueOf(workingRule));
                        if(!workingNode.hasChoice()){
                            actualDependency.remove(Integer.valueOf(workingRule));
                            clashList.remove(Integer.valueOf(workingRule));
                        }
                        //workingNode =nodeList.get(workingRule);
                        ArrayList<OWLClassExpression> flag = new ArrayList<>(Abox.subList(0,workingNode.getAbox().size()));
                        Abox.removeAll(Abox);
                        Abox.addAll(flag);
                        //System.out.println("NEW ABOX:"+Abox);
                        workingRule = clashList.get(clashList.size()-1);
                        backtrack();
                    }
                    else 
                        workingRule = -1;
                }

            }
            /*else{

                for (Integer i: nodeList.keySet()) {
                    if(i>workingRule)
                        nodeList.remove(nodeList.get(i));

                }

                Abox.removeAll(Collections.unmodifiableList(Abox));
                Abox.addAll(workingNode.getAbox());
                dependency = dependency.subList(0,Abox.size());
                for (int i = 0; i<dependency.size();i++) {

                    ArrayList<Integer> l = new ArrayList<>(dependency.get(i));
                    for (Integer j: l) {

                        if(j > workingRule)
                            l.remove(j);
                        else if(j == workingRule && !actualDependency.contains(workingRule))
                            l.remove(j);

                    }
                    dependency.set(i,l);

                }

                for (int i = actualDependency.size()-1; i>=0; i--){
                    if(actualDependency.get(i)>workingRule)
                        actualDependency.remove(actualDependency.get(i));
                }
                Set<OWLObjectPropertyExpression> listSome = someRelation.keySet();
                for (OWLObjectPropertyExpression oe : listSome) {

                    ArrayList<DynamicTableau> t = new ArrayList<>(someRelation.remove(oe));

                    if(t.size() != 0){

                        for (int i = t.size() - 1; i >=0 ; i--) {
                            if(t.get(i).getParent() > workingRule)
                                t.remove(i);
                        }

                        if(t.size()!=0)
                            someRelation.put(oe,t);
                    }
                }
            }*/
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
                    dependency.add(Abox.size(),new ArrayList<>(actualDependency));
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
                    //LoggerManager.writeDebugLog("CLASH "+ OntologyRenderer.render(c) + " "+ OntologyRenderer.render(c1), DynamicTableau.class);
                    clashList = new ArrayList<>(dependency.get(i));
                    for (Integer j:dependency.get(i1)) {
                        if(!clashList.contains(j)){
                            clashList.add(j);
                        }


                    }
                    Collections.sort(clashList);
                    LoggerManager.writeDebugLog("CLASH "+ clashList, DynamicTableau.class);
                    //Abox.remove(c1);
                    return true;
                }
            }
        }
        return false;

    }

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
                    List<DynamicTableau> related = someRelation.get(oe);
                    for (DynamicTableau t : related) {
                        model=model.concat(" EXIST " + OntologyRenderer.render((oe)) + ". {");
                        t.getModel();
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

    public Integer getIteration(){

        int it=iteration;

        Set<OWLObjectPropertyExpression> listSome = someRelation.keySet();

        for (OWLObjectPropertyExpression oe : listSome) {

            List<DynamicTableau> lt = someRelation.get(oe);

            for (DynamicTableau t: lt) {
                it+=t.getIteration();

            }
        }
        return it;
    }
}




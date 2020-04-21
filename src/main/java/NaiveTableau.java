import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import java.lang.reflect.Array;
import java.util.*;

public class NaiveTableau implements Tableau{

    private List<OWLClassExpression> Abox;

    private List<Node> branchingNode;

    private Node workingNode;

    private List<Node> nodeList;

    private int workingRule;

    private Map<OWLObjectPropertyExpression, List<NaiveTableau>> directSelf;

    private int parent;

    private int modelLength;

    ShortFormProvider shortFormProvider = new
            SimpleShortFormProvider();
    OWLObjectRenderer renderer = new
            ManchesterOWLSyntaxOWLObjectRendererImpl();



    protected NaiveTableau(OWLClassExpression concept, int parent) {

        this.parent = parent;
        Abox = new ArrayList<>();
        Abox.add(Abox.size(), concept);
        branchingNode = new ArrayList<>();
        directSelf= new HashMap<>();
        nodeList = new ArrayList<>();
        modelLength = 1;
        renderer.setShortFormProvider(shortFormProvider);


    }


    private boolean checkClash() {

        for (int i = 0; i < Abox.size(); i++) {

            OWLClassExpression c = Abox.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < Abox.size(); i1++) {

                OWLClassExpression c1 = Abox.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    //System.out.println("CLASH*********** "+ renderer.render(c) + " " +renderer.render(c1));

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
        //System.out.println("INTERSECTION "+ renderer.render(Abox.get(workingRule)));
        workingNode = new Node(Abox, workingRule);
        checkIntersection(workingNode.applyRule());
        modelLength = Abox.size()-1;
        //System.out.println("MODEL LENGht"+ modelLength);
        nodeList.add(workingRule, workingNode);
        workingRule++;
    }

    private void applyUnion(){
        //System.out.println("UNION " + renderer.render(Abox.get(workingRule)));
        if(branchingNode.size()!=0 && branchingNode.get(branchingNode.size()-1).getWorkingRule()==workingRule) {
            workingNode = branchingNode.get(branchingNode.size()-1);
        } else{
            workingNode = new Node(Abox, workingRule);
            branchingNode.add(branchingNode.size(),workingNode);
        }
        List<OWLClassExpression> choice = workingNode.applyRule();
        if(choice.get(0)!=null) {
            //System.out.println("CHOICE " + renderer.render(choice.get(0)));
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
        //System.out.println("SOME: "+ workingRule + " " + renderer.render(rule));

        NaiveTableau direct;
        OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();

        //VERIFICO SE INDIVIDUO HA LA RELAZIONE
        List<NaiveTableau> related = directSelf.get(oe);
        direct = new NaiveTableau(filler, workingRule);
        if (related == null) {

            if(direct.SAT()){
                directSelf.put(oe, Collections.singletonList(direct));
                workingRule++;
            }
            else{
                //System.out.println("SOME FALLITO");

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

                    ArrayList<NaiveTableau> flag = new ArrayList<>(directSelf.get(oe));
                    flag.add(direct);
                    directSelf.put(oe, flag);
                    workingRule++;
                }
                else{
                    //System.out.println("SOME FALLITO");
                    workingRule--;
                    backtrack();
                }
            } else
                workingRule++;
        }
    }

    private void applyAll(OWLClassExpression rule){
        //System.out.println("ALL "+ workingRule + " " + renderer.render(rule));

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        List<NaiveTableau> related = directSelf.get(oe);
        if (related == null){

                workingRule++;
            //backtrack();
        }
        else{
            
            for (NaiveTableau t: related){

                if(!t.checkAll(filler)){
                    workingRule -=  modelLength;
        //System.out.println("ALL FALLITO");
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
        //System.out.println("BACKTRACK :" + workingRule);

        if(branchingNode.size()!=0) {
            workingNode = nodeList.get(workingRule);
            nodeList = nodeList.subList(0, workingRule);
            Abox.removeAll(Abox);
            Abox.addAll(workingNode.getAbox());
            for (OWLObjectPropertyExpression oe : directSelf.keySet()) {
                List<NaiveTableau> t = directSelf.get(oe);
                if(t!= null && t.size()!=0){
                    for (int i = t.size() - 1 ; i >=0 ; i--) {
                        if(t.get(i).getParent() > workingRule)
                            t = t.subList(0, i);
                    }
                    directSelf.put(oe,t);
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


    public void printModel(){
        for (OWLClassExpression e: Abox) {
            if(e != null) {
                ShortFormProvider shortFormProvider = new
                        SimpleShortFormProvider();
                OWLObjectRenderer renderer = new
                        ManchesterOWLSyntaxOWLObjectRendererImpl();
                renderer.setShortFormProvider(shortFormProvider);
                ClassExpressionType pe = e.getClassExpressionType();
                switch (pe) {
                    case OWL_CLASS:
                    case OBJECT_COMPLEMENT_OF:
                        System.out.print(" " + renderer.render((e)) + " |");
                        break;
                }
            }
        }
        Set<OWLObjectPropertyExpression> key =  directSelf.keySet();

        for (OWLObjectPropertyExpression oe: key) {
            if(oe != null) {
                ShortFormProvider shortFormProvider = new
                        SimpleShortFormProvider();
                OWLObjectRenderer renderer = new
                        ManchesterOWLSyntaxOWLObjectRendererImpl();
                renderer.setShortFormProvider(shortFormProvider);
                List<NaiveTableau> related = directSelf.get(oe);
                System.out.print(" EXIST " + renderer.render((oe)) + ".");
                for (NaiveTableau t : related) {
                    t.printModel();
                }
            }
        }
    }
}




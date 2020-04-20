import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import java.util.*;

public class NaiveTableau implements Tableau{

    public List<OWLClassExpression> Abox;

    public List<Node> branchingNode;

    private Node workingNode;

    public List<Node> nodeList;

    public int workingRule;

    private Map<OWLObjectPropertyExpression, List<NaiveTableau>> directSelf;

    private int parent;



    protected NaiveTableau(OWLClassExpression concept, int parent) {

        this.parent = parent;
        Abox = new ArrayList<>();
        Abox.add(Abox.size(), concept);
        branchingNode = new ArrayList<>();
        directSelf= new HashMap<>();
        nodeList = new ArrayList<>();
    }


    public boolean checkClash() {

        for (int i = 0; i < Abox.size(); i++) {

            OWLClassExpression c = Abox.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < Abox.size(); i1++) {

                OWLClassExpression c1 = Abox.get(i1);

                if (c.equals(c1.getComplementNNF()))
                    return true;
            }
        }
        return false;

    }


    public boolean checkSome(OWLClassExpression expression) {

            return Abox.contains(expression);

    }


    public boolean checkAll(OWLClassExpression expression) {

        if(!Abox.contains(expression)){
            ArrayList<OWLClassExpression> flag = new ArrayList<>(Abox);
            Abox.add(Abox.size(),expression);
            boolean result = SAT();
            Abox = flag;
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
                            while( workingRule != branchingNode.get(branchingNode.size()-1).getWorkingRule()) {
                                Abox.remove(Abox.size() - 1);
                                workingRule--;
                            }
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
        //System.out.println("INTERSECTION");
        Node node = new Node(Abox, workingRule);
        workingNode = node;
        checkIntersection(node.applyRule());
        workingRule++;
    }

    private void applyUnion(){
        //System.out.println("UNION");
        Node node;
        if(branchingNode.size()!=0 && branchingNode.get(branchingNode.size()-1).getWorkingRule()==workingRule) {
            node = branchingNode.get(branchingNode.size()-1);
        } else{
            node = new Node(Abox, workingRule);
            branchingNode.add(branchingNode.size(),node);
        }
        workingNode = node;
        List<OWLClassExpression> choice = node.applyRule();
        if(choice.get(0)!=null) {
            checkIntersection(choice);
            if (!checkClash())
                workingRule++;
            else
                Abox.remove(Abox.size() - 1);
        }
        else{
            branchingNode.remove(node);
            Abox.remove(Abox.size() - 1);
            workingRule--;
        }
    }

    private void applySome(OWLClassExpression rule){
        //System.out.println("SOME");

        NaiveTableau direct;
        OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();

        //VERIFICO SE INDIVIDUO HA LA RELAZIONE
        List<NaiveTableau> related = directSelf.get(oe);
        if (related == null) {

            direct = new NaiveTableau(filler, workingRule);
            if(direct.SAT()){
                directSelf.put(oe, Collections.singletonList(direct));
                workingRule++;
            }
            else
                workingRule--;
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
                direct = new NaiveTableau(filler, workingRule);
                if(direct.SAT()) {
                    List<NaiveTableau> flag = directSelf.get(oe);
                    flag.add(direct);
                    workingRule++;
                }
                else
                    workingRule--;
            } else
                workingRule++;
        }
    }

    private void applyAll(OWLClassExpression rule){
        //System.out.println("ALL");

        OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();
        boolean check = true;

        List<NaiveTableau> related = directSelf.get(oe);
        if (related == null)
            workingRule++;
        else{
            
            for (NaiveTableau t: related){

                if(!t.checkAll(filler)){
                    workingRule = t.parent;
                    backtrack();
                    check = false;
                    break;
                }
            }

            if(check)
                workingRule++;

        }

    }

    private void backtrack() {
        workingNode = nodeList.get(workingRule);
        Abox = workingNode.getAbox();

    }

    private boolean isWorking() {
        return !((workingRule>=Abox.size()) || (workingRule<0));

    }

    public void checkIntersection(List<OWLClassExpression> disjointedList){
        nodeList.add(nodeList.size(), workingNode);
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




import org.semanticweb.owlapi.model.*;

import java.util.*;

public class NaiveTableau implements Tableau{

    public List<OWLClassExpression> Abox;

    public List<Node> branchingNode;

    public int workingRule;

    private Map<OWLObjectPropertyExpression, List<NaiveTableau>> directSelf;

    private int parent;



    protected NaiveTableau(OWLClassExpression concept, int parent) {

        this.parent = parent;
        Abox = new ArrayList<>();
        Abox.add(Abox.size(), concept);
        branchingNode = new ArrayList<>();
        directSelf= new HashMap<>();
    }


    public boolean checkClash() {

        for (int i = 0; i < Abox.size(); i++) {

            OWLClassExpression c = Abox.get(i);

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
            Abox.add(Abox.size(),expression);
            return SAT();
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
                    applyIntersection(rule);
                    break;
                case OBJECT_UNION_OF:
                    applyUnion(rule);
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

    private void applyIntersection(OWLClassExpression rule){
        System.out.println("INTERSECTION");
        Node node = new Node(rule, workingRule);
        checkIntersection(node.applyRule());
        workingRule++;
    }

    private void applyUnion(OWLClassExpression rule){
        System.out.println("UNION");

        Node node;
        if(branchingNode.size()!=0 && branchingNode.get(branchingNode.size()-1).getWorkingRule()==workingRule) {
            node = branchingNode.get(branchingNode.size()-1);
        } else{
            node = new Node(rule, workingRule);
            branchingNode.add(branchingNode.size(),node);
        }
        List<OWLClassExpression> choice = node.applyRule();
        if(choice!=null) {
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
        System.out.println("SOME");

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
                    directSelf.put(oe, Collections.singletonList(direct));
                    workingRule++;
                }
                else
                    workingRule--;
            } else
                workingRule++;
        }
    }

    private void applyAll(OWLClassExpression rule){
        System.out.println("ALL");

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
                    directSelf.get(oe).remove(t);
                    workingRule = t.parent;
                    check = false;
                    break;
                }
            }

            if(check)
                workingRule++;

        }

    }

    private boolean isWorking() {
        return !((workingRule>=Abox.size()) || (workingRule<0));

    }

    public void checkIntersection(List<OWLClassExpression> disjointedList){

        if(disjointedList!=null) {
            for (OWLClassExpression ce: disjointedList ) {
                if(!Abox.contains(ce))
                    Abox.add(Abox.size(),ce);


            }
        }
    }


    public void printModel(){
        for (OWLClassExpression e: Abox) {
            ClassExpressionType pe = e.getClassExpressionType();
            switch (pe){
                case OWL_CLASS:
                case OBJECT_COMPLEMENT_OF:
                    System.out.println(e.toString()+ " ");
                    break;
            }
        }

        Set<OWLObjectPropertyExpression> key =  directSelf.keySet();

        for (OWLObjectPropertyExpression oe: key) {
            List<NaiveTableau> related = directSelf.get(oe);
            System.out.println("EXISTENTIAL RELATION " + oe.toString());
                for (NaiveTableau t : related) {
                    t.printModel();
                }
        }
    }
}




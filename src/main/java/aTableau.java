import org.semanticweb.owlapi.model.*;

import java.util.*;

public class aTableau implements Tableau{



    /**
     * Default constructor
     * @param concept
     * @param type
     */
    protected aTableau(OWLClassExpression concept, OWLObjectPropertyExpression type, int parent) {


        this.Concept = concept;
        this.type = type;
        this.parent = parent;
        Abox = new ArrayList<OWLClassExpression>();
        Abox.add(Abox.size(),Concept);
        disjointNode = new ArrayList<aNode>();
        nodeList = new ArrayList<aNode>();
        directSelf= new HashMap<OWLObjectPropertyExpression, List<aTableau>>();


    }



    private OWLClassExpression Concept;

    private OWLObjectPropertyExpression type;

    /**
     *
     */
    public List<OWLClassExpression> Abox;

    /**
     *
     */
    public List<aNode> disjointNode;

    /**
     *
     */
    public int workingRule;

    private List<aNode> nodeList;

    private Map<OWLObjectPropertyExpression, List<aTableau>> directSelf;

    private int parent;





    /**
     * @return
     */
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

    /**
     *
     */
    public void backtrack() {
        // TODO implement here
    }

    /**
     * @param expression
     * @return
     */
    public boolean checkSome(OWLClassExpression expression) {

            return Abox.contains(expression);

    }

    /**
     * @param expression
     * @return
     */
    public boolean checkAll(OWLClassExpression expression) {

        if(!Abox.contains(expression)){
            Abox.add(Abox.size(),expression);
            return SAT();
        }
        return true;
    }

    /**
     * @return
     */
    public boolean SAT() {


        while(isWorking()){
            List<OWLClassExpression> newRule = new ArrayList<OWLClassExpression>();
            OWLClassExpression rule = Abox.get(workingRule);
            ClassExpressionType type = rule.getClassExpressionType();
            aNode node = null;
            switch (type) {
                case OBJECT_INTERSECTION_OF:
                    System.out.println("INTERSECTION");
                    node = new aNode(rule, workingRule);
                    checkIntersection(node.applyRule());
                    workingRule++;
                    break;
                case OBJECT_UNION_OF:
                    System.out.println("UNION");

                    node = new aNode(rule, workingRule);
                    if(disjointNode.contains(node))
                        disjointNode.add(disjointNode.size(),node);
                    OWLClassExpression choice = node.applyChoice();
                    if(choice!=null) {
                        checkIntersection(Collections.singletonList(choice));
                        if (!checkClash())
                            workingRule++;
                        else {
                            Abox.remove(Abox.size() - 1);
                        }
                    }
                    else{
                        disjointNode.remove(node);
                        workingRule--;
                    }
                    break;
                case OBJECT_SOME_VALUES_FROM:
                    System.out.println("SOME");

                    aTableau direct = null;
                    OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
                    OWLObjectPropertyExpression oe = someValue.getProperty();
                    OWLClassExpression filler = someValue.getFiller();

                    //VERIFICO SE INDIVIDUO HA LA RELAZIONE 
                    List<aTableau> related = directSelf.get(oe);
                    if (related == null) {

                        direct = new aTableau(filler, oe, workingRule);
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

                        for (aTableau t : related) {

                                if (t.checkSome(filler)) {
                                    check = true;
                                    break;
                                }

                            }
                            if (!check) {
                                //CASO IN CUI NESSUNO DEI NODI CON QUESTA RELAZIONE HA LA FORMULA TRA IL SUO RULE SET
                                //QUINDI INSTANZIO NUOVO INDIVIDUO E MI SALVO LA RELAZIONE
                                direct = new aTableau(filler, oe, workingRule);
                                if(direct.SAT()) {
                                    directSelf.put(oe, Collections.singletonList(direct));
                                    workingRule++;
                                }
                                else
                                    workingRule--;
                            } else
                                workingRule++;
                        }
                    break;
                case OBJECT_ALL_VALUES_FROM:
                    System.out.println("ALL");

                    OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
                    OWLClassExpression fillers = allValue.getFiller();
                    OWLObjectPropertyExpression oea = allValue.getProperty();
                    boolean checK = true;

                    List<aTableau> relateda = directSelf.get(oea);
                    if (relateda == null)
                        workingRule++;
                    else{

                        List<aTableau> directs = directSelf.get(oea);

                        for (aTableau t: directs){

                            if(!t.checkAll(fillers)){
                                directSelf.get(oea).remove(t);
                                workingRule = t.parent;
                                checK = false;
                                break;
                            }
                        }

                        if(checK)
                            workingRule++;

                    }
                   break;
                case OWL_CLASS:
                    System.out.println("CLASS");

                    if(checkClash())
                        workingRule--;
                    else
                        workingRule++;
                    break;

                case OBJECT_COMPLEMENT_OF:
                    System.out.println("COMPLEMENT OF");
                    if(checkClash())
                        workingRule--;
                    else
                        workingRule++;
                    break;

            }

        }
        if(workingRule<=0)
            return false;
        return true;
    }

    private boolean isWorking() {
        return !((workingRule>=Abox.size()) || (workingRule<0));

    }

    public void checkIntersection(List<OWLClassExpression> disjointedList){

        if(disjointedList!=null)
        {for (OWLClassExpression ce: disjointedList ) {
            if(Abox.contains(ce) == false)
                Abox.add(Abox.size(),ce);
        }}
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
            List<aTableau> related = directSelf.get(oe);
            System.out.println("EXISTENTIAL RELATION " + oe.toString());
                for (aTableau t : related) {
                    t.printModel();
                }
        }
    }
}




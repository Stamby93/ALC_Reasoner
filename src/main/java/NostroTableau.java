import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class NostroTableau implements Tableau {

    /**
     *
     */
    private List<List<NostroNode>> individualList;

    /**
     *
     */
    private List<Map<OWLObjectPropertyExpression, List<Integer>>> relation;

    /**
     * Default constructor
     */
    private NostroTableau() {
    }


    /**
     *
     */
    private OWLClassExpression Concept;

    private int workingRule = 0;

    private int pendent = 0;

    private int individual = 0;


    /**
     * @return
     */
    public boolean SAT() {
        NostroNode root = individualList.get(individual).get(0);
        boolean result = false;
        NostroNode working = root;
        List<NostroNode> workingList = null;
        while (isWorking()) {

            workingList = individualList.get(working.getID());
            result = working.applyRule();

            if (result && !working.isAll() && !working.isSome()) {

                working = new NostroNode(working.getRuleSet(), working.getRule()+1, working.getID());
                workingList.add(workingList.size() - 1, working);
            } else if (!result) {
                if (backtrack(working) == false) {
                    individualList.remove(working.getID());
                    if (individualList.isEmpty())
                        return false;
                    working = getNext();
                }
            } else if (working.isSome()) {

                OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) working.getQuantifier();
                OWLClassExpression filler = someValue.getFiller();
                OWLObjectPropertyExpression oe = someValue.getProperty();

                NostroNode some = new NostroNode(Collections.singletonList(filler), 0, individual++);

                List<Integer> related = null;
                List<NostroNode> tree = null;
                //VERIFICO SE INDIVIDUO HA RELAZIONI
                if (relation.get(working.getID()) != null) {

                    Map<OWLObjectPropertyExpression, List<Integer>> flag = relation.get(working.getID());
                    related = flag.get(oe);
                    //CASO NON ESISTE RELAZIONE RICHIESTA
                    if (related == null) {
                        flag.put(oe, Collections.singletonList(some.getID()));
                        working = some;
                        individualList.add(some.getID(), Collections.singletonList(some));

                    }
                    //CASO IN CUI RELAZIONE RICHIESTA ESISTE, VERIFICO SE E' PRESENTE LA REGOLA NEL RULE SET
                    else {
                        boolean check = false;

                        for (Integer i : related) {

                            tree = individualList.get(i);
                            List<OWLClassExpression> rules = tree.get(tree.size() - 1).getRuleSet();
                            if (!rules.contains(filler))
                                check = false;
                            else {
                                check = true;
                                break;
                            }

                        }
                        if (check) {
                            //CASO IN CUI NESSUNO DEI NODI CON QUESTA RELAZIONE HA LA FORMULA TRA IL SUO RULE SET
                            //QUINDI INSTANZIO NUOVO INDIVIDUO E MI SALVO LA RELAZIONE
                            related.add(related.size() - 1, some.getID());
                            individualList.add(some.getID(), Collections.singletonList(some));
                            working = some;
                        } else
                            working = getNext();

                    }
                }
                //CASO IN CUI L'INDIVIDUO NON AVEVA RELAZIONI
                else{

                        Map<OWLObjectPropertyExpression, List<Integer>> map = new HashMap<OWLObjectPropertyExpression, List<Integer>>();
                        map.put(oe, Collections.singletonList(some.getID()));
                        relation.set(working.getID(), map);
                        individualList.add(some.getID(), Collections.singletonList(some));
                        working = some;
                    }
            }
            else if (working.isAll()) {

                    OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) working.getQuantifier();
                    OWLClassExpression filler = allValue.getFiller();
                    OWLObjectPropertyExpression oe = allValue.getProperty();
                    if (relation.get(working.getID())==null) {
                        working = getNext();
                    } else {
                        List<Integer> re = null;
                        re = relation.get(working.getID()).get(oe);
                        if(re!= null) {
                            for (Integer i : re) {
                                List<NostroNode> l = individualList.get(i);
                                NostroNode n = l.get(l.size() - 1);
                                List<OWLClassExpression> le = new ArrayList<OWLClassExpression>();
                                le.addAll(n.getRuleSet());
                                l.add(l.size(), new NostroNode(le, n.getRule(), n.getID()));
                            }
                        }
                        working = getNext();


                    }

                }
/*        for (OWLClassExpression e: root.RuleSet
             ) {

            System.out.println("ROOT" + e.toString());
        }
*/
            }

        return result;
    }

    private NostroNode getNext(){
            NostroNode n = null;
            for (List<NostroNode> l : individualList) {
                n = l.get(l.size() - 1);
                if (n.isWorking())
                    return n;

            }
            return null;
        }

    /**
     * @param Concept
     * @return
     */
    public NostroTableau(OWLClassExpression Concept) {
        if (Concept == null)
            throw new NullPointerException("Input concept must not be null");
        this.Concept = Concept;
        individualList = new ArrayList<List<NostroNode>>();


        individualList.add(0, Collections.singletonList(new NostroNode(Collections.singletonList(Concept), workingRule, individual)));

        relation = new ArrayList<Map<OWLObjectPropertyExpression, List<Integer>>>();
    }
/*
    /**
     * @param node
     * @return
     */

    /*
    private boolean applyRule(NostroNode node) {
        OWLClassExpression rule = node.getWorkingRule();
        ClassExpressionType type = rule.getClassExpressionType();
        List<Integer> nodes = null;
        switch (type) {
            case OBJECT_INTERSECTION_OF:
                //System.out.println("Intersection");
                OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) rule;
                List<OWLClassExpression> disjointedList = intersection.operands().collect(Collectors.toList());
                node.checkIntersection(disjointedList);
                return true;
            case OBJECT_UNION_OF:
                //System.out.println("Union");

                OWLObjectUnionOf union = (OWLObjectUnionOf) rule;
                List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
                node.setBranch(jointedList);
                if (!node.applyChoice())
                    return node.backtrack();
                return true;
            case OBJECT_SOME_VALUES_FROM:
                //System.out.println("Some");

                OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
                OWLClassExpression filler = someValue.getFiller();
                OWLObjectPropertyExpression oe = someValue.getProperty();
                if(!node.checkRelation(oe)) {
                    NostroNodeNEW = new Node(filler);
                    individualList.add(individualList.size(), NEW);
                    node.addRelation(oe, individualList.size()-1);
                    if(applyRule(NEW) == false)
                        return NEW.backtrack();
                    node.ruleApplied();
                    return true;
                }
                else{
                    nodes = node.getConnectedBy(oe);
                    for (Integer i: nodes){
                        individualList.get(i).addRule(filler);
                        if(applyRule(individualList.get(i)) == false){
                            if(individualList.get(i).backtrack() == false)
                                return false;
                        }
                    }
                    node.ruleApplied();
                    return true;
                }
            case OBJECT_ALL_VALUES_FROM:
                //System.out.println("All");

                OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
                OWLClassExpression fillerAll = allValue.getFiller();
                OWLObjectPropertyExpression oeAll = allValue.getProperty();
                if(node.checkRelation(oeAll)) {
                    nodes = node.getConnectedBy(oeAll);
                    for (Integer i : nodes) {
                        individualList.get(i).addRule(fillerAll);
                        if (applyRule(individualList.get(i)) == false) {
                            if (individualList.get(i).backtrack() == false)
                                return false;
                        }
                    }
                    node.ruleApplied();
                    return true;
                }
                node.ruleApplied();
                return false;//node.backtrack();
            case OWL_CLASS:
                //System.out.println("Class");

                if(node.checkClash())
                    return node.backtrack();
                node.ruleApplied();
                return true;
            case DATA_SOME_VALUES_FROM:
                break;
            case DATA_ALL_VALUES_FROM:
                break;
        }

        return false;
    }

*/

    private boolean backtrack(NostroNode working) {

        return false;
    }

    private boolean isWorking() {
        return false;
    }





}


import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 */
public class NaiveTableau implements Tableau {

    /**
     *
     */
    private List<Node> NodeList;

    /**
     * Default constructor
     */
    private NaiveTableau() {
    }


    /**
     *
     */
    private OWLClassExpression Concept;


    /**
     * @return
     */
    public boolean SAT() {
        Node root = NodeList.get(0);
        boolean result = false;
        int i = 0;
        while (root.isWorking()) {
            result = applyRule(root);

        }
/*        for (OWLClassExpression e: root.RuleSet
             ) {

            System.out.println("ROOT" + e.toString());
        }
*/
        return result;
    }

    /**
     * @param Concept
     * @return
     */
    public NaiveTableau(OWLClassExpression Concept) {
        if (Concept == null)
            throw new NullPointerException("Input concept must not be null");
        this.Concept = Concept;
        NodeList = new ArrayList<Node>();
        NodeList.add(0, new Node(Concept));
    }

    /**
     * @param node
     * @return
     */
    private boolean applyRule(Node node) {
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
                    Node NEW = new Node(filler);
                    NodeList.add(NodeList.size(), NEW);
                    node.addRelation(oe, NodeList.size()-1);
                    if(applyRule(NEW) == false)
                        return NEW.backtrack();
                    node.ruleApplied();
                    return true;
                }
                else{
                    nodes = node.getConnectedBy(oe);
                    for (Integer i: nodes){
                        NodeList.get(i).addRule(filler);
                        if(applyRule(NodeList.get(i)) == false){
                            if(NodeList.get(i).backtrack() == false)
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
                        NodeList.get(i).addRule(fillerAll);
                        if (applyRule(NodeList.get(i)) == false) {
                            if (NodeList.get(i).backtrack() == false)
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



    /**
     * @return
     */
    private int chooseRule() {
        // TODO implement here
        return 0;
    }

}
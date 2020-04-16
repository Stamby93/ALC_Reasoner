
import org.semanticweb.owlapi.model.*;

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
        while (root.isWorking()) {
            result = applyRule(root);
        }

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
        switch (type) {
            case OBJECT_INTERSECTION_OF:
                OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) rule;
                List<OWLClassExpression> disjointedList = intersection.operands().collect(Collectors.toList());
                node.checkIntersection(disjointedList);
                return true;
            case OBJECT_UNION_OF:
                OWLObjectUnionOf union = (OWLObjectUnionOf) rule;
                List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
                node.setBranch(jointedList);
                if (!node.applyChoice())
                    return node.backtrack();
                return true;
            case OBJECT_SOME_VALUES_FROM:
                OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
                OWLClassExpression filler = someValue.getFiller();
                //VERIFICARE LE PREMESSE PRIMA DI PROCEDERE CON L'APPLICAZIONE
                NodeList.add(NodeList.size(), new Node(filler));
                node.addRelation(someValue.getProperty(), NodeList.size());
                break;
            case OBJECT_ALL_VALUES_FROM:
                OWLObjectAllValuesFrom allValue = (OWLObjectAllValuesFrom) rule;
                break;
            case OWL_CLASS:
                break;
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
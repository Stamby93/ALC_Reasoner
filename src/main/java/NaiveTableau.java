
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
        // TODO implement here
        return false;
    }

    /**
     * @param Concept 
     * @return
     */
    public NaiveTableau(OWLClassExpression Concept) {
        if(Concept == null)
            throw new NullPointerException("Input concept must not be null");
        this.Concept = Concept;
        NodeList = new ArrayList<Node>();
    }

    /**
     * @param node
     */
    private void backtrack(Node node) {
        // TODO implement here
    }

    /**
     * @param node 
     * @return
     */
    private void applyRule(Node node) {
        OWLClassExpression rule = node.getWorkingRule();
        ClassExpressionType type = rule.getClassExpressionType();
        switch (type){
            case OBJECT_INTERSECTION_OF:
                OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) rule;
                List<OWLClassExpression> disjointedList = intersection.operands().collect(Collectors.toList());
                node.checkIntersection(disjointedList);
                break;
            case OBJECT_UNION_OF:
                OWLObjectUnionOf union = (OWLObjectUnionOf) rule;
                List<OWLClassExpression> jointedtList = union.operands().collect(Collectors.toList());

                //VERIFICARE LE PREMESSE PRIMA DI PROCEDERE CON L'APPLICAZIONE
                node.setBranch(jointedtList);
                break;
            case OBJECT_SOME_VALUES_FROM:
                OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom) rule;
                OWLClassExpression filler = someValue.getFiller();
                //VERIFICARE LE PREMESSE PRIMA DI PROCEDERE CON L'APPLICAZIONE
                NodeList.add(NodeList.size(),new Node(filler));
                node.addRelation(someValue.getProperty(),NodeList.size());
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


    }

    /**
     * @return
     */
    private int chooseRule() {
        // TODO implement here
        return 0;
    }

}
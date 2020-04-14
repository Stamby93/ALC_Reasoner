
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.*;

/**
 * 
 */
public class DynamicTableau implements Tableau {

    /**
     * Default constructor
     */
    public DynamicTableau() {
    }


    /**
     * 
     */
    private Node working;

    /**
     * 
     */
    private Node root;

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
    public DynamicTableau DynamicTableau(OWLClassExpression Concept) {
        // TODO implement here
        return null;
    }

    /**
     * @param conflictSet 
     * @return
     */
    private void backtrack(Set<AboxItem> conflictSet) {
        // TODO implement here
    }

    /**
     * @param r 
     * @return
     */
    private void applyRule(AboxItem r) {
        // TODO implement here
    }

    /**
     * @return
     */
    private AboxItem chooseRule() {
        // TODO implement here
        return null;
    }

    /**
     * @param conflictSet 
     * @param b
     */
    private void clearConflictSet(Set<AboxItem> conflictSet, AboxItem b) {
        // TODO implement here
    }

}
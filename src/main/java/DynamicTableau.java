
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 */
public class DynamicTableau implements Tableau {

    /**
     * Default constructor
     */
    public DynamicTableau() {
        DynamicTableau(null);
    }



    /**
     * 
     */
    private OWLClassExpression Concept;

    /**
     *
     */

    private Set<OWLClassExpression> RuleSet;

    private Set<OWLClass> Abox;

    private int []  choise;


    /**
     * @return
     */
    public boolean SAT() {
        return false;
    }

    /**
     * @param Concept 
     * @return
     */
    public DynamicTableau DynamicTableau(OWLClassExpression Concept) {
        if(Concept == null)
            throw new NullPointerException("Input concept must be not null");
        this.Concept = Concept;
        return this;
    }

    /**
     * @param conflictSet 
     * @return
     */
    private void backtrack(Stack<Integer> conflictSet) {
        // TODO implement here
    }

    /**
     * @param r 
     * @return
     */
    private void applyRule(int r) {
        // TODO implement here
    }

    /**
     * @return
     */
    private int chooseRule() {
    return 0;
    }

}
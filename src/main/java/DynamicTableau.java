package main.java;

import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.*;

/**
 * 
 */
public class DynamicTableau implements Tableau {
    Stack T;
    Set<AboxItem> conflictSet; //STACK<NodeBranch>??????
    Set<AboxItem> R;
    OWLClassExpression workConcept;

    /**
     * Default constructor
     */
    public DynamicTableau() {

    }
    public DynamicTableau(OWLClassExpression concept){
        //workConcept = NULL;
        T = new Stack();
        //R = NULL; //USA CONCEPT IN INPUT
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
        while (! R.isEmpty()) {
            AboxItem r = chooseRule();
            applyRule(r);
            if(hasClash(workConcept))
                backtrack(conflictSet);
        }
        return true;
    }

    private boolean hasClash(OWLClassExpression concept) {
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
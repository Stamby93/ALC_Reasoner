import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Sub node.
 */
public class subNode extends Node {

    /**
     * Instantiates a new Sub node.
     *
     * @param operand     the operand
     * @param workingRule the working rule
     */
    public subNode(List<OWLClassExpression> operand, int workingRule) {
        super(operand,workingRule);

    }

    /**
     * Alter rule.
     *
     * @param o    the o
     * @param rule the rule
     */
    public void alterRule(OWLClassExpression o, int rule){
        super.oldAbox.set(rule,o);
    }


    /**
     * Update status.
     *
     * @param toDelete the to delete
     * @param toUpdate the to update
     * @param rule     the rule
     */
    public void updateStatus(List<Integer> toDelete, int toUpdate, OWLClassExpression rule){

        oldAbox.set(toUpdate,rule);
        ArrayList<OWLClassExpression> newAbox = new ArrayList<>(oldAbox);
        int shiftWorkingRule = 0;
        for (Integer i = 0; i < toDelete.size() && toDelete.get(i) < oldAbox.size(); i++) {

            if(toDelete.get(i) < workingRule)
                shiftWorkingRule++;

            newAbox.remove(oldAbox.get(toDelete.get(i)));

        }
        workingRule-=shiftWorkingRule;
        oldAbox.removeAll(oldAbox);
        oldAbox.addAll(newAbox);

    }

}

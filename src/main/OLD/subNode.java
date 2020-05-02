import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.List;

/**
 * The type Sub node.
 */
public class subNode extends myNode {

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

}

import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.List;

public class subNode extends myNode {

    public subNode(List<OWLClassExpression> operand, int workingRule) {
        super(operand,workingRule);

    }

    public void alterRule(OWLClassExpression o, int rule){
        super.oldAbox.set(rule,o);
    }

}

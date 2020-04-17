import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import java.util.List;
import java.util.stream.Collectors;


public class aNode {



    public aNode(OWLClassExpression operand, int workingRule) {
        this.Operand = operand;
        this.workingRule = workingRule;

    }


    private int workingRule;

    /**
     *
     */
    private int currentChoice = 0;

    private OWLClassExpression Operand;



    /**
     * @return
     */
    public OWLClassExpression applyChoice() {
        OWLObjectUnionOf union = (OWLObjectUnionOf) Operand;
        List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
        currentChoice++;
        return jointedList.get(currentChoice-1);
    }

    /**
     * @return
     */
    public List<OWLClassExpression> applyRule() {
        OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) Operand;
        List<OWLClassExpression> disjointedList = intersection.operands().collect(Collectors.toList());
        return disjointedList;
    }

}

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import java.util.List;
import java.util.stream.Collectors;


public class Node {



    public Node(OWLClassExpression operand, int workingRule) {
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
        if(currentChoice - 1 < jointedList.size())
            return jointedList.get(currentChoice-1);
        return null;
    }

    /**
     * @return
     */
    public List<OWLClassExpression> applyRule() {
        OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) Operand;
        List<OWLClassExpression> disjointedList = intersection.operands().collect(Collectors.toList());
        return disjointedList;
    }

    public int getWorkingRule(){
        return workingRule;
    }

}

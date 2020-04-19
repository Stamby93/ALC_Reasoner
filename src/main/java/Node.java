import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import java.util.Collections;
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


    public List<OWLClassExpression> applyRule(){
        ClassExpressionType type = Operand.getClassExpressionType();
        switch (type){
            case OBJECT_INTERSECTION_OF:
                return applyIntersection();
            case OBJECT_UNION_OF:
                return Collections.singletonList(applyChoice());
        }

        return null;

    }
    /**
     * @return
     */
    private OWLClassExpression applyChoice() {
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
    private List<OWLClassExpression> applyIntersection() {
        OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) Operand;
        List<OWLClassExpression> disjointedList = intersection.operands().collect(Collectors.toList());
        return disjointedList;
    }

    public int getWorkingRule(){
        return workingRule;
    }

}

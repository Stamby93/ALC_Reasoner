import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The type Old node.
 */
public class oldNode {

    private final int workingRule;

    private int currentChoice = 0;

    /**
     * The Abox.
     */
    protected final List<OWLClassExpression> Abox;

    private List<OWLClassExpression> jointedList;


    /**
     * Instantiates a new Old node.
     *
     * @param operand     the operand
     * @param workingRule the working rule
     */
    public oldNode(List<OWLClassExpression> operand, int workingRule) {
        this.Abox = new ArrayList<>(operand);
        this.workingRule = workingRule;

    }

    /**
     * Apply rule list.
     *
     * @return the list
     */
    public List<OWLClassExpression> applyRule() {
        ClassExpressionType type = Abox.get(workingRule).getClassExpressionType();
        switch (type) {
            case OBJECT_INTERSECTION_OF:
                jointedList = new ArrayList<>();
                return applyIntersection();
            case OBJECT_UNION_OF:
                return Collections.singletonList(applyChoice());
        }

        return null;

    }


    private OWLClassExpression applyChoice() {
        OWLObjectUnionOf union = (OWLObjectUnionOf) Abox.get(workingRule);
        jointedList = union.operands().collect(Collectors.toList());
        currentChoice++;
        if (currentChoice - 1 < jointedList.size())
            return jointedList.get(currentChoice - 1);
        return null;
    }


    private List<OWLClassExpression> applyIntersection() {
        OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) Abox.get(workingRule);
        return intersection.operands().collect(Collectors.toList());
    }


    /**
     * Gets abox.
     *
     * @return the abox
     */
    public List<OWLClassExpression> getAbox() {
        return Abox;
    }

    /**
     * Has choice boolean.
     *
     * @return the boolean
     */
    public boolean hasChoice() {
        return currentChoice < jointedList.size();
    }
}

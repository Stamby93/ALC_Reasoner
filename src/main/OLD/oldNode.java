import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class oldNode {

    private final int workingRule;

    private int currentChoice = 0;

    protected final List<OWLClassExpression> Abox;

    private List<OWLClassExpression> jointedList;



    public oldNode(List<OWLClassExpression> operand, int workingRule) {
        this.Abox = new ArrayList<>(operand);
        this.workingRule = workingRule;

    }

    public List<OWLClassExpression> applyRule(){
        ClassExpressionType type = Abox.get(workingRule).getClassExpressionType();
        switch (type){
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
        if(currentChoice - 1 < jointedList.size())
            return jointedList.get(currentChoice-1);
        return null;
    }


    private List<OWLClassExpression> applyIntersection() {
        OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) Abox.get(workingRule);
        return intersection.operands().collect(Collectors.toList());
    }


    public List<OWLClassExpression> getAbox() {
        return Abox;
    }

    public boolean hasChoice(){
        return currentChoice < jointedList.size();
    }
}

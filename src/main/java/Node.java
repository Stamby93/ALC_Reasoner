import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class Node {

    private final int workingRule;

    private int currentChoice = 0;

    private final List<OWLClassExpression> Abox;



    public Node(List<OWLClassExpression> operand, int workingRule) {
        this.Abox = new ArrayList<>(operand);
        this.workingRule = workingRule;

    }


    public List<OWLClassExpression> applyRule(){
        ClassExpressionType type = Abox.get(workingRule).getClassExpressionType();
        switch (type){
            case OBJECT_INTERSECTION_OF:
                return applyIntersection();
            case OBJECT_UNION_OF:
                return Collections.singletonList(applyChoice());
        }
        System.out.println("NODE: " + currentChoice);


        return null;

    }


    private OWLClassExpression applyChoice() {
        OWLObjectUnionOf union = (OWLObjectUnionOf) Abox.get(workingRule);
        List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
        currentChoice++;
        if(currentChoice - 1 < jointedList.size())
            return jointedList.get(currentChoice-1);
        return null;
    }


    private List<OWLClassExpression> applyIntersection() {
        OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) Abox.get(workingRule);
        return intersection.operands().collect(Collectors.toList());
    }


    public int getWorkingRule(){
        return workingRule;
    }

    public List<OWLClassExpression> getAbox() {
        return Abox;
    }
}

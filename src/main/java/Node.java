import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Node implements Tableau{

    private final int workingRule;

    private int currentChoice = 0;

    private List<OWLClassExpression> Abox;

    protected final List<OWLClassExpression> oldAbox;

    protected Node(List<OWLClassExpression> operand, int workingRule) {
        this.oldAbox = new ArrayList<>(operand);
        this.workingRule = workingRule;

    }

    @Override
    public boolean SAT() {

        ClassExpressionType type = oldAbox.get(workingRule).getClassExpressionType();
        switch (type){
            case OBJECT_INTERSECTION_OF:
                if(currentChoice == 0 ){
                    currentChoice++;
                    Abox = new ArrayList<>(oldAbox);
                    OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) Abox.get(workingRule);
                    List<OWLClassExpression> operand = intersection.operands().collect(Collectors.toList());
                    for (OWLClassExpression e: operand) {
                        if(!Abox.contains(e))
                            Abox.add(Abox.size(),e);
                    }
                    return true;
                }
                return false;
            case OBJECT_UNION_OF:
                Abox = new ArrayList<>(oldAbox);
                OWLObjectUnionOf union = (OWLObjectUnionOf) Abox.get(workingRule);
                List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
                if(currentChoice < jointedList.size()) {
                    if(!Abox.contains(jointedList.get(currentChoice))) {
                        Abox.add(Abox.size(),jointedList.get(currentChoice).getNNF());
                    }
                    currentChoice++;
                    return true;
                }
                return false;
        }

        return false;
    }

    @Override
    public List<OWLClassExpression> getAbox() {
        return Abox;
    }

    @Override
    public int getParent(){
        return workingRule;
    }

    @Override
    public String getModel() {
        return null;
    }

    @Override
    public Integer getIteration() {
        return null;
    }

    public boolean hasChoice(){
        OWLObjectUnionOf union = (OWLObjectUnionOf) Abox.get(workingRule);
        List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
        return currentChoice < jointedList.size();
    }

}

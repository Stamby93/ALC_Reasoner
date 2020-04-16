
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.*;

/**
 * 
 */
public class Node {

    /**
     * Default constructor
     */
    private Node() {
    }

    /**
     *
     */
    public Node(OWLClassExpression unexpandedRule){
        if(unexpandedRule == null)
            throw new NullPointerException("Rule for new node must be not null");
        this.RuleSet = new ArrayList<OWLClassExpression>();
        this.RuleSet.add(workingRule,unexpandedRule);
    }


    /**
     * 
     */
    public Map<OWLObjectPropertyExpression, List<Integer>> relation;

    /**
     * 
     */
    public List<List<OWLClassExpression>> choice;

    /**
     * 
     */
    public int workingRule = 0;

    /**
     *
     */
    private List<OWLClassExpression> RuleSet;

    public OWLClassExpression getWorkingRule(){
        return RuleSet.get(workingRule);
    }

    public void setBranch(List<OWLClassExpression> operands){
        if(operands == null)
            throw new NullPointerException("Branch alternatives must be not null");
        choice.add(workingRule,operands);
    }

    public void addRelation(OWLObjectPropertyExpression pe, int i){
        if(relation.containsKey(pe)){
            relation.get(pe).add(i);
        }
        else{
            relation.put(pe, new ArrayList<Integer>(i));
        }
    }
}

import org.semanticweb.owlapi.model.OWLClass;
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
        this.choice = new HashMap<Integer, List<OWLClassExpression>>();
    }


    /**
     * 
     */
    public Map<OWLObjectPropertyExpression, List<Integer>> relation;

    /**
     * 
     */
    public Map<Integer, List<OWLClassExpression>> choice;

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
        choice.put(workingRule,operands);
    }

    public void addRelation(OWLObjectPropertyExpression pe, int i){
        if(relation.containsKey(pe)){
            relation.get(pe).add(i);
        }
        else{
            relation.put(pe, new ArrayList<Integer>(i));
        }
    }

    public boolean checkRelation(OWLObjectPropertyExpression pe){
        return relation.containsKey(pe);
    }

    public List<Integer> getConnectedBy(OWLObjectPropertyExpression pe){
        return relation.get(pe);
    }

    public void setChoice(List<OWLClassExpression> jointedList){

        choice.put(workingRule,jointedList);

    }

    public void addRule(OWLClassExpression e){
        if(!RuleSet.contains(e))
            RuleSet.add(RuleSet.size(),e);
    }

    public boolean applyChoice() {

        List<OWLClassExpression> eList = choice.get(workingRule);

        if(eList.size() != 0){
            OWLClassExpression e = eList.get(eList.size());
            eList.remove(eList.size());
            RuleSet.set(RuleSet.size(),e);
            if(checkClash())
                return false;
            workingRule++;
            return true;
        }

        return false;
    }

    private boolean checkClash() {

        for (int i = 0; i < RuleSet.size(); i++) {

            OWLClassExpression c = RuleSet.get(i);

            for (int i1 = i+1; i1 < RuleSet.size(); i1++) {

                OWLClassExpression c1 = RuleSet.get(i1);

                if (c.equals((OWLClassExpression) c1.getObjectComplementOf())) {

                    return true;
                }
            }
        }
        return false;

    }

    public void checkIntersection(List<OWLClassExpression> disjointedList){

        for (OWLClassExpression ce: disjointedList ) {
            if(RuleSet.contains(ce) == false)
                RuleSet.add(RuleSet.size(),ce);
        }
    }

    public boolean isWorking(){
        return !(workingRule>RuleSet.size());
    }

    public boolean backtrack() {
        return false;
    }
}


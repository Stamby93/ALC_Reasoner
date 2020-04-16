
import org.eclipse.rdf4j.model.vocabulary.OWL;
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
        this.relation = new HashMap<OWLObjectPropertyExpression, List<Integer>>();
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
    public List<OWLClassExpression> RuleSet;



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


    public void addRule(OWLClassExpression e){
        if(!RuleSet.contains(e))
            RuleSet.add(RuleSet.size()-1,e);
    }

    public boolean applyChoice() {

        List<OWLClassExpression> eList = choice.get(workingRule);

        if(eList.size() != 0){
            OWLClassExpression e = eList.get(eList.size()-1);
            eList.remove(eList.size()-1);
            RuleSet.add(RuleSet.size(),e);
            if(checkClash())
                return false;
            ruleApplied();
            return true;
        }

        return false;
    }

    public boolean checkClash() {

        for (int i = 0; i < RuleSet.size(); i++) {

            OWLClassExpression c = RuleSet.get(i);

            for (int i1 = i+1; i1 < RuleSet.size(); i1++) {

                OWLClassExpression c1 = RuleSet.get(i1);

                if (c.equals(c1.getComplementNNF()))
                    return true;
            }
        }
        return false;

    }

    public void checkIntersection(List<OWLClassExpression> disjointedList){

        for (OWLClassExpression ce: disjointedList ) {
            if(RuleSet.contains(ce) == false)
                RuleSet.add(RuleSet.size(),ce);
        }
        ruleApplied();
    }

    public boolean isWorking(){
        return (!(workingRule>=RuleSet.size()) && (workingRule!=-1));
    }

    public void ruleApplied(){
        workingRule++;
    }

    public boolean backtrack() {

        while(workingRule>=0){
            workingRule--;
            RuleSet.remove(RuleSet.size()-1);
            if(choice.get(workingRule)!= null && !choice.get(workingRule).isEmpty())
                return true;
        }

        return false;
    }
}


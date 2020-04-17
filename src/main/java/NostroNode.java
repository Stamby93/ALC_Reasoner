import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.*;
import java.util.stream.Collectors;


public class NostroNode {

    /**
     * Default constructor
     */
    private NostroNode() {
    }

    private int id;

    /**
     *
     */
    public NostroNode(List<OWLClassExpression> unexpandedRule, int workingRule, int id){
        if(unexpandedRule == null)
            throw new NullPointerException("Rule for new node must be not null");
        this.RuleSet = new ArrayList<OWLClassExpression>();
        this.RuleSet = unexpandedRule;
        this.workingRule = workingRule;
        this.id= id;
    }


    private boolean isSome = false;
    private boolean isAll = false;



    /**
     *
     */
    private Map<Integer, List<OWLClassExpression>> choice;

    /**
     *
     */
    private int workingRule = 0;

    /**
     *
     */
    private List<OWLClassExpression> RuleSet;

    public int getID(){
        return id;
    }


    public boolean isAll(){
        return isAll;
    }

    public boolean isSome(){
        return isSome;
    }

    public int getWorkingRule(){
        return workingRule;
    }

    public OWLClassExpression getQuantifier(){
        return RuleSet.get(workingRule);
    }

    public List<OWLClassExpression> getRuleSet(){
        return RuleSet;
    }


    public boolean applyRule(){

        //return applyRule(RuleSet.get(workingRule));
        return false;
    }



    private boolean applyRule(OWLObjectIntersectionOf rule){
        List<OWLClassExpression> disjointedList = rule.operands().collect(Collectors.toList());
        checkIntersection(disjointedList);
        return true;
    }

    private boolean applyRule(OWLObjectUnionOf rule){
        this.choice = new HashMap<Integer, List<OWLClassExpression>>();
        List<OWLClassExpression> jointedList = rule.operands().collect(Collectors.toList());
        setBranch(jointedList);
        if (!applyChoice())
            return false;
        return true;

    }

    private boolean applyRule(OWLObjectSomeValuesFrom rule){
        isSome=true;
        return true;
    }

    private boolean applyRule(OWLObjectAllValuesFrom rule){
        isAll=true;
        return true;
    }
/*
    public OWLClassExpression getWorkingRule(){
        return RuleSet.get(workingRule);
    }
*/
    private void setBranch(List<OWLClassExpression> operands){
        if(operands == null)
            throw new NullPointerException("Branch alternatives must be not null");
        choice.put(workingRule,operands);
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

    public int getRule() {
        return workingRule;
    }
}



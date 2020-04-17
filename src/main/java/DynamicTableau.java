
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 */
public class DynamicTableau implements Tableau {

    /**
     * Default constructor
     */
    public DynamicTableau() {
        DynamicTableau(null);
    }


    /**
     * 
     */
    private OWLClassExpression Concept;

    /**
     *
     */
    private List<OWLClassExpression> RuleSet;

    /**
     *
     */
    private List<OWLClass> Abox;

    /**
     *
     */

    private List<Integer> choice;

    /**
     *
     */

    private Map<OWLClass,Stack<Integer>> dependecies;


    /**
     *
     */

    private int currentRule = 0;

    /**
     *
     */

    private Stack<Integer> branchingPoint;




    /**
     * @return
     */
    public boolean SAT() {

        Abox = new ArrayList<OWLClass>();
        dependecies = new HashMap<OWLClass, Stack<Integer>>();
        choice = new ArrayList<Integer>();
        branchingPoint = new Stack<Integer>();

        int rule = 0;
        Stack<Integer> conflictSet;

        while (currentRule!= RuleSet.size()){
            rule = chooseRule();
            applyRule(rule);
            conflictSet = hasClash();
            if(conflictSet != null)
                if(backtrack(conflictSet) == false)
                    return false;
        }

        return true;
    }

    @Override
    public void printModel() {

    }

    private Stack<Integer> hasClash() {

        Stack<Integer> result = null;

        for (int i = 0; i < Abox.size(); i++) {

            OWLClass c = Abox.get(i);

            for (int i1 = 0; i1 < Abox.size(); i1++) {

                OWLClass c1 = Abox.get(i1);

                if (c.equals((OWLClass) c1.getObjectComplementOf())) {
                    Stack<Integer> first = dependecies.get(c);
                    Stack<Integer> second = dependecies.get(c1);

                    result = new Stack<Integer>();
                    result.addAll(first);
                    result.addAll(second);

                    Collections.sort(result, Collections.reverseOrder());
                    break;
                }
            }

            if(result != null)
                break;
        }

        return result;
    }

    /**
     * @param Concept 
     * @return
     */
    public DynamicTableau DynamicTableau(OWLClassExpression Concept) {
        if(Concept == null)
            throw new NullPointerException("Input concept must not be null");
        this.Concept = Concept;
        RuleSet.add(currentRule, Concept);
        return this;
    }

    /**
     * @param conflictSet 
     * @return
     */
    private boolean backtrack(Stack<Integer> conflictSet) {
        if(conflictSet == null || conflictSet.size() == 0)
            return false;
        Integer b = conflictSet.pop();
        List<OWLClass> toDelete = new LinkedList<OWLClass>();
        for (OWLClass c: Abox) {
            Stack<Integer> flag = dependecies.get(c);
            if(flag.contains(b))
                toDelete.add(c);
        }
        Abox.removeAll(toDelete);

        if(choice.get(b.intValue()) != 0){
            return true; // DA CONTINUARE
        }
        else
            return backtrack(conflictSet);
    }

    /**
     * @param r 
     * @return
     */
    private void applyRule(int r) {
        OWLClassExpression rule = RuleSet.get(r);
        ClassExpressionType type = rule.getClassExpressionType();
        switch (type){
            case OWL_CLASS:
                break;
            case OBJECT_SOME_VALUES_FROM:
                break;
            case OBJECT_ALL_VALUES_FROM:
                break;
            case DATA_SOME_VALUES_FROM:
                break;
            case DATA_ALL_VALUES_FROM:
                break;
            case OBJECT_INTERSECTION_OF:
                OWLObjectIntersectionOf castRule = (OWLObjectIntersectionOf) rule;
                Set<OWLClassExpression> operands = castRule.operands().collect(Collectors.toSet());
                choice.set(r, operands.size());
                if(!Abox.containsAll(operands))
                    //Abox.addAll(operands);
                break;
            case OBJECT_UNION_OF:
                break;
        }

    }

    /**
     * @return
     */
    private int chooseRule() {
        currentRule++;
        return currentRule;
    }

}
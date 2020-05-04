package ALC_Reasoner;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Chronological ALC_Reasoner.Tableau is a class that implements the ALC_Reasoner.Tableau interface.
 * The reasoning technique is the basic one, in particular during the backtrack
 * phase we always return to the last concept that generated a branch.
 */
public class ChronologicalTableau implements Tableau{

    /**
     * The Concept list.
     * Is a list of OWLClassExpression that will contain the expansions
     * of the various rules. It is initialized with the input concept.* @link <a href="http://owlcs.github.io/owlapi/apidocs_5/index.html">OWLClassExpression</a>
     */
    private final List<OWLClassExpression> conceptList;

    /**
     * The Working rule.
     * An int variable that tracks the current rule during reasoning.
     */

    private int workingRule = 0;

    /**
     * The Some relation.
     * A map that keeps track of existential quantifiers encountered during reasoning.
     * The keySet is of type OWLObjectPropertyExpression, the valueSet is a list of integers.
     * So passing the relationship type this object return a list of pointers to the {@link #conceptList}.
     */

    private final Map<OWLObjectPropertyExpression, List<Integer>> someRelation;

    /**
     * The All relation.
     * A map that keeps track of universal quantifiers encountered during reasoning.
     * The keySet is of type OWLObjectPropertyExpression, the valueSet is a list of integers.
     * So passing the relationship type this object return a list of pointers to the {@link #conceptList}.
     */

    private final Map<OWLObjectPropertyExpression, List<Integer>> allRelation;

    /**
     * The Iterations.
     * An int variable that acts as a counter for the number of iterations needed to complete the reasoning.
     */

    private int iteration = 0;

    /**
     * The Concept comparator.
     * It is an object used to reorder objects of type OWLClassExpression.
     * The order relation is as follows:
     * "OBJECT_INTERSECTION_OF {@literal <} OBJECT_UNION_OF {@literal <} OBJECT_SOME_VALUES_FROM {@literal <} OBJECT_ALL_VALUES_FROM {@literal <} OBJECT_COMPLEMENT_OF {@literal <=} OWL_CLASS"
     */
    private final Comparator<? super OWLClassExpression> conceptComparator;

    /**
     * Instantiates a new Chronological tableau.
     *
     * @param concept OWLClassExpression The input concept.
     * @param parent  int A int value used to keep track during the reasoning of existential quantifiers.
     */
    protected ChronologicalTableau(@Nonnull OWLClassExpression concept, int parent) {

        conceptList = new ArrayList<>();
        conceptList.add(0, concept);
        someRelation = new HashMap<>();
        allRelation = new HashMap<>();
        conceptComparator = (Comparator<OWLClassExpression>) (expression, t1) -> {

            ClassExpressionType type = expression.getClassExpressionType();
            switch (type) {
                case OBJECT_INTERSECTION_OF:
                    if (t1.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
                        return 0;
                    }
                    return -1;

                case OBJECT_UNION_OF:
                    switch (t1.getClassExpressionType()) {
                        case OBJECT_INTERSECTION_OF:
                            return 1;
                        case OBJECT_UNION_OF:
                            return 0;
                        default:
                            return -1;
                    }
                case OBJECT_SOME_VALUES_FROM:
                    switch (t1.getClassExpressionType()) {
                        case OBJECT_INTERSECTION_OF:
                        case OBJECT_UNION_OF:
                            return 1;
                        case OBJECT_SOME_VALUES_FROM:
                            return 0;
                        default:
                            return -1;
                    }

                case OBJECT_ALL_VALUES_FROM:
                    switch (t1.getClassExpressionType()) {
                        case OBJECT_INTERSECTION_OF:
                        case OBJECT_UNION_OF:
                        case OBJECT_SOME_VALUES_FROM:
                            return 1;
                        case OBJECT_ALL_VALUES_FROM:
                            return 0;
                        default:
                            return -1;
                    }
                case OWL_CLASS:
                case OBJECT_COMPLEMENT_OF:
                    switch (t1.getClassExpressionType()) {
                        case OWL_CLASS:
                        case OBJECT_COMPLEMENT_OF:
                            return 0;
                        default:
                            return 1;
                    }

            }
            return -1;
        };

        LoggerManager.writeDebugLog("SAT: "+ parent, ChronologicalTableau.class);

    }

    @Override
    public boolean SAT() {

        if(workingRule <= conceptList.size() -1) {
            OWLClassExpression rule = conceptList.get(workingRule);
            ClassExpressionType type = rule.getClassExpressionType();
            switch (type) {
                case OBJECT_INTERSECTION_OF:
                    return applyIntersection((OWLObjectIntersectionOf)rule);
                case OBJECT_UNION_OF:
                    return applyUnion((OWLObjectUnionOf)rule);
                case OBJECT_SOME_VALUES_FROM:
                    return applySome((OWLObjectSomeValuesFrom)rule);
                case OBJECT_ALL_VALUES_FROM:
                    return applyAll((OWLObjectAllValuesFrom)rule);
                case OWL_CLASS:
                case OBJECT_COMPLEMENT_OF:
                    LoggerManager.writeDebugLog("Rule: "+ workingRule + " CLASS :" + OntologyRenderer.render(conceptList.get(workingRule)), JumpingTableau.class);

                    iteration++;
                    if (checkClash())
                        return false;

                    workingRule++;
                    return SAT();
            }

        }
        return true;

    }

    /**
     *This method performs the operation of exhaustively applying the intersection rule.
     * @param intersection OWLObjectIntersectionOf The intersection to be solved.
     * @return True if after the application of the intersection a recursive call to {@link #SAT()} return true,
     * false otherwise.
     */

    private boolean applyIntersection(@Nonnull OWLObjectIntersectionOf intersection){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(intersection), ChronologicalTableau.class);

        List<OWLClassExpression> operand = intersection.operands().sorted(conceptComparator).collect(Collectors.toList());
        for (OWLClassExpression owlClassExpression : operand) {
            if (!conceptList.contains(owlClassExpression))
                conceptList.add(conceptList.size(), owlClassExpression);
        }

        iteration++;
        workingRule ++;
        return SAT();
    }

    /**
     * This method performs the operation of exhaustively applying the union rule.
     * @param union OWLObjectUnionOf The union to be solved.
     * @return True if after the application of the union a recursive call to {@link #SAT()} return true,
     * false otherwise.*/

    private boolean applyUnion(@Nonnull OWLObjectUnionOf union){
        LoggerManager.writeDebugLog("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(union), ChronologicalTableau.class);

        int rule = workingRule;
        List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
        ArrayList<OWLClassExpression> saveT = new ArrayList<>(conceptList);
        OWLClassExpression owlClassExpression;

        jointedList.sort(conceptComparator);

        for (int i = 0; i < jointedList.size(); i++) {

            iteration++;
            owlClassExpression = jointedList.get(i);

            if (!conceptList.contains(owlClassExpression)) {
                LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(owlClassExpression), ChronologicalTableau.class);

                conceptList.add(conceptList.size(), owlClassExpression);

                if (checkClash())
                    conceptList.remove(conceptList.size() - 1);
                else {

                    workingRule++;

                    if (i == jointedList.size()-1)
                        return SAT();

                    if(SAT())
                        return true;

                    LoggerManager.writeDebugLog("BACKTRACK " + rule, ChronologicalTableau.class);

                    workingRule = rule;
                    cleanRelation(someRelation);
                    cleanRelation(allRelation);
                    conceptList.removeAll(Collections.unmodifiableList(conceptList));
                    conceptList.addAll(saveT);

                }
            }
        }

        //NON HO PIÙ SCELTE
        return false;

    }

    /**
     * This method performs the operation of applying the existential rule.
     * If there are no conditions for the application of the rule, it does not apply it, and
     * the method return the value of a recursive call to {@link #SAT()}.
     * @param someValue OWLObjectSomeValuesFrom The existential quantifier to apply.
     * @return True if after the application of the quantifier a recursive call to {@link #SAT()} return true,
     * false otherwise.
     */

    private boolean applySome(@Nonnull OWLObjectSomeValuesFrom someValue){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(someValue), ChronologicalTableau.class);

        Tableau direct;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();

        List<Integer> related = new ArrayList<>();
        //VERIFICO SE INDIVIDUO HA LA RELAZIONE QUESTO
        if(someRelation.get(oe)!=null)
            related.addAll(someRelation.get(oe));

        //CASO IN CUI RELAZIONE RICHIESTA ESISTE, VERIFICO SE E' PRESENTE LA REGOLA NELLA CONCEPT LIST
        if (related.size()!=0) {

            OWLObjectSomeValuesFrom flag;
            for (Integer r : related) {

                flag = (OWLObjectSomeValuesFrom) conceptList.get(r);

                if(filler.equals(flag.getFiller())){
                    LoggerManager.writeDebugLog("SOME ALREADY PRESENT", ChronologicalTableau.class);

                    workingRule++;
                    iteration ++;
                    return SAT();

                }
            }
        }
        //CASO IN CUI INDIVIDUO O NON HA LA RELAZIONE O
        //NESSUNO DEI INDIVIDUI CON QUESTA RELAZIONE HA LA FORMULA TRA LA SUA CONCEPT LIST
        //QUINDI INSTANZIO NUOVO INDIVIDUO E MI SALVO LA RELAZIONE

        if(allRelation.get(oe)!=null){

            //CREO INTERSEZIONE DEI CONCETTI CONTENUTI NEGLI ESISTENZIALI DI QUESTO TIPO

            OWLObjectAllValuesFrom allRule;

            ArrayList<OWLClassExpression> operands = new ArrayList<>();

            for (Integer i: allRelation.get(oe)) {

                allRule = (OWLObjectAllValuesFrom)conceptList.get(i);
                operands.add(allRule.getFiller());

            }

            operands.add(filler);
            operands.sort(conceptComparator);
            filler = new OWLObjectIntersectionOfImpl(operands);

        }

        direct = new ChronologicalTableau(filler, workingRule);
        if(direct.SAT()) {
            LoggerManager.writeDebugLog("SOME "+workingRule+" SATISFIABLE", ChronologicalTableau.class);

            related.add(related.size(),workingRule);
            someRelation.put(oe, related);
            workingRule++;
            iteration += direct.getIteration();
            return SAT();

        }
        else{
            LoggerManager.writeDebugLog("SOME UNSATISFIABLE", ChronologicalTableau.class);

            iteration += direct.getIteration();
            return false;

        }

    }

    /**
     * This method performs the operation of applying the universal rule.
     * If there are no conditions for the application of the rule, it does not apply it, and
     * the method return the value of a recursive call to {@link #SAT()}.
     * @param allValue OWLObjectAllValuesFrom The universal quantifier to apply.
     * @return True if after the application of the quantifier a recursive call to {@link #SAT()} return true,
     * false otherwise.
     */

    private boolean applyAll(@Nonnull OWLObjectAllValuesFrom allValue){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(allValue), ChronologicalTableau.class);

        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();

        if (someRelation.get(oe) == null){
            LoggerManager.writeDebugLog("ALL NO CONDITIONS", ChronologicalTableau.class);

            iteration++;

        }
        else{

            ArrayList<Integer> related = new ArrayList<>(someRelation.get(oe));
            ArrayList<OWLClassExpression> allRules = new ArrayList<>();
            ArrayList<OWLClassExpression> operands;
            OWLObjectSomeValuesFrom flag;

            allRules.add(filler);

            if(allRelation.get(oe)!=null){

                OWLObjectAllValuesFrom allRule;

                for (Integer j: allRelation.get(oe)) {

                    allRule = (OWLObjectAllValuesFrom) conceptList.get(j);
                    allRules.add(allRule.getFiller());

                }

                allRules.sort(conceptComparator);

            }

            for (Integer integer : related) {

                flag = (OWLObjectSomeValuesFrom) conceptList.get(integer);

                if (!filler.equals(flag.getFiller())) {

                    operands = new ArrayList<>();
                    operands.add(flag.getFiller());
                    operands.addAll(allRules);
                    operands.sort(conceptComparator);

                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);
                    Tableau Tflag = new ChronologicalTableau(concept, workingRule);

                    if (!Tflag.SAT()) {
                        LoggerManager.writeDebugLog("ALL UNSATISFIABLE", ChronologicalTableau.class);

                        iteration+=Tflag.getIteration();
                        return false;

                    }
                    LoggerManager.writeDebugLog("ALL "+workingRule+" SATISFIABLE", ChronologicalTableau.class);

                    iteration+=Tflag.getIteration();
                }
            }

        }

        if(allRelation.get(oe) == null)
            allRelation.put(oe,Collections.singletonList(workingRule));
        else{

            ArrayList<Integer> l = new ArrayList<>(allRelation.get(oe));
            l.add(l.size(),workingRule);
            allRelation.put(oe,l);

        }
        workingRule++;
        return SAT();

    }

    /**
     * This method is used to restore relationship maps to the state of the {@link #workingRule}.
     * @param relation Map&lt;OWLObjectPropertyExpression, List&lt;Integer&gt;&lt;
     */

    private void cleanRelation(@Nonnull Map<OWLObjectPropertyExpression, List<Integer>> relation){
        Set<OWLObjectPropertyExpression> list = relation.keySet();
        for (OWLObjectPropertyExpression oe : list) {

            ArrayList<Integer> t = new ArrayList<>(relation.remove(oe));


            for (int i = t.size() - 1; i >=0 ; i--) {
                if(t.get(i) > workingRule)
                    t.remove(i);
            }

            if(t.size()!=0)
                relation.put(oe,t);

        }

    }

    /**
     * Scroll the {@link #conceptList} until it finds a clash or the list is finish.
     * @return boolean True if {@link #conceptList} contain a contradiction, false otherwise.
     */

    private boolean checkClash() {

        for (int i = 0; i < conceptList.size(); i++) {

            OWLClassExpression c = conceptList.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < conceptList.size(); i1++) {

                OWLClassExpression c1 = conceptList.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    LoggerManager.writeDebugLog("CLASH "+ OntologyRenderer.render(c) + " | " +OntologyRenderer.render(c1), ChronologicalTableau.class);
                    return true;
                }
            }
        }
        return false;

    }

    @Override
    public String getModel(){
        String model = "| ";
        for (OWLClassExpression e: conceptList) {
            if(e != null) {
                ClassExpressionType pe = e.getClassExpressionType();
                switch (pe) {
                    case OWL_CLASS:
                    case OBJECT_COMPLEMENT_OF:
                    case OBJECT_SOME_VALUES_FROM:
                    case OBJECT_ALL_VALUES_FROM:

                        model=model.concat(OntologyRenderer.render((e))+ " | ");

                        break;
                }
            }
        }

        return model;
    }

    @Override
    public Integer getIteration(){return iteration;}

}




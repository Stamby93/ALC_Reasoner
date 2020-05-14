package ALC_Reasoner;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Jumping tableau.
 * as suggested by the name this is the implementation of the tableau which makes use of the Jumpig (or BackJumping) Backtrack
 * as described in Efficient and generic reasoning for modal logics Z.Li - 2008
 * @link <a href="http://staff.cs.manchester.ac.uk/~schmidt/mltp/Thesis.pdf">Efficient and generic reasoning for modal logics Z.Li - 2008</a>
 *
 */
public class JumpingTableau extends ChronologicalTableau{

    /**
     * The dependency.
     * This list contains, for each element of the {@link #conceptList}., a list of integers corresponding to the dependencies of that concept.
     */
    protected final List<List<Integer>> dependency;

    /**
     * The clashList.
     * The list created after a clash occurs.
     */

    protected List<Integer> clashList;


    /**
     * Instantiates a new Jumping tableau.
     *
     * @param concept the concept
     * @param parent  the parent
     */
    protected JumpingTableau(OWLClassExpression concept, int parent) {

        super(concept,parent);
        dependency = new ArrayList<>();
        dependency.add(0,Collections.singletonList(-1));

    }

    /**
     * This method update the {@link #dependency}, whith the rule, for the elements from start to end
     * @param start first element to update
     * @param end last element to update
     * @param rule the new dependecies for the elements
     */

    protected void addDependency(int start, int end, List<Integer> rule){

        for(int i = start; i < end; i++)
            dependency.add(i,new ArrayList<>(rule));

    }

    @Override
    protected boolean applyIntersection(OWLObjectIntersectionOf intersection){

        List<OWLClassExpression> operand = intersection.operands().sorted(conceptComparator).collect(Collectors.toList());
        int i = 0;
        for (OWLClassExpression owlClassExpression : operand) {
            if (!conceptList.contains(owlClassExpression)){
                conceptList.add(conceptList.size(), owlClassExpression);
                i++;
            }
        }
        if(i!=0)
            addDependency(conceptList.size() - i,conceptList.size() , dependency.get(workingRule));
        iteration++;
        workingRule ++;
        return SAT();
    }

    @Override
    protected boolean applyUnion(OWLObjectUnionOf union){

        int rule = workingRule;
        List<OWLClassExpression> jointedList = union.operands().collect(Collectors.toList());
        ArrayList<OWLClassExpression> saveT = new ArrayList<>(conceptList);
        ArrayList<List<Integer>> saveTD = new ArrayList<>(dependency);
        ArrayList<Integer> dep = new ArrayList<>();
        dep.add(workingRule);
        OWLClassExpression owlClassExpression;

        jointedList.sort(conceptComparator);

        for (int i = 0; i < jointedList.size(); i++) {

            iteration++;
            owlClassExpression = jointedList.get(i);

            if (!conceptList.contains(owlClassExpression)) {

                conceptList.add(conceptList.size(), owlClassExpression);

                addDependency(conceptList.size() - 1, conceptList.size(), dep);

                if (checkClash()){

                    conceptList.remove(conceptList.size() - 1);
                    dependency.remove(dependency.size()-1);

                }
                else {

                    workingRule++;
                    if (i == jointedList.size()-1)
                        return SAT();

                    if(SAT())
                        return true;
                    else if(!clashList.contains(rule))
                        return false;


                    workingRule = rule;
                    cleanRelation(someRelation);
                    cleanRelation(allRelation);
                    conceptList.removeAll(Collections.unmodifiableList(conceptList));
                    conceptList.addAll(saveT);
                    dependency.removeAll(Collections.unmodifiableList(dependency));
                    dependency.addAll(saveTD);

                }
                //AGGIORNO DIPENDENZE PER IL PROSSIMO CONGIUNTO
                for (Integer c: clashList ) {

                    if(!dep.contains(c))
                        dep.add(c);

                }

                Collections.sort(dep);
            }
        }

        clashList.remove(Integer.valueOf(rule));

        return false;

    }

    @Override
    protected boolean applySome(OWLObjectSomeValuesFrom someValue){

        Tableau direct;
        OWLObjectPropertyExpression oe = someValue.getProperty();
        OWLClassExpression filler = someValue.getFiller();
        clashList = new ArrayList<>();

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
                for (Integer d: dependency.get(i)) {

                    if(!clashList.contains(d))
                        clashList.add(d);

                }
            }

            operands.add(filler);
            operands.sort(conceptComparator);
            filler = new OWLObjectIntersectionOfImpl(operands);

        }

        direct = new JumpingTableau(filler, workingRule);
        if(direct.SAT()) {

            related.add(related.size(),workingRule);
            someRelation.put(oe, related);
            workingRule++;
            iteration += direct.getIteration();
            return SAT();

        }
        else{

            iteration += direct.getIteration();
            for (Integer d: dependency.get(workingRule)) {

                if(!clashList.contains(d))
                    clashList.add(d);

            }
            Collections.sort(clashList);
            return false;

        }

    }

    @Override
    protected boolean applyAll(OWLObjectAllValuesFrom allValue){

        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();

        if (someRelation.get(oe) == null)
            iteration++;
        else{

            ArrayList<Integer> related = new ArrayList<>(someRelation.get(oe));
            ArrayList<OWLClassExpression> allRules = new ArrayList<>();
            OWLObjectSomeValuesFrom flag;
            clashList = new ArrayList<>();

            allRules.add(filler);

            if(allRelation.get(oe)!=null){

                OWLObjectAllValuesFrom allRule;

                for (Integer j: allRelation.get(oe)) {

                    allRule = (OWLObjectAllValuesFrom) conceptList.get(j);
                    allRules.add(allRule.getFiller());
                    for (Integer d: dependency.get(j)) {

                        if(!clashList.contains(d))
                            clashList.add(d);

                    }

                }

                allRules.sort(conceptComparator);

            }

            for (Integer integer : related) {

                flag = (OWLObjectSomeValuesFrom) conceptList.get(integer);

                if (!filler.equals(flag.getFiller())) {

                    ArrayList<OWLClassExpression> operands = new ArrayList<>();
                    operands.add(flag.getFiller());
                    operands.addAll(allRules);
                    operands.sort(conceptComparator);

                    OWLObjectIntersectionOf concept = new OWLObjectIntersectionOfImpl(operands);
                    Tableau Tflag = new JumpingTableau(concept, workingRule);

                    if (!Tflag.SAT()) {

                        iteration += Tflag.getIteration();

                        for (Integer d: dependency.get(workingRule)) {

                            if(!clashList.contains(d))
                                clashList.add(d);

                        }

                        for (Integer d: dependency.get(integer)) {

                            if(!clashList.contains(d))
                                clashList.add(d);

                        }

                        Collections.sort(clashList);

                        return false;

                    }

                    iteration += Tflag.getIteration();

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

    @Override
    protected boolean checkClash() {

        clashList = new ArrayList<>();

        for (int i = 0; i < conceptList.size(); i++) {

            OWLClassExpression c = conceptList.get(i);

            if(c.isOWLNothing())
                return true;

            for (int i1 = i+1; i1 < conceptList.size(); i1++) {

                OWLClassExpression c1 = conceptList.get(i1);

                if (c.equals(c1.getComplementNNF())){
                    clashList.addAll(dependency.get(i));
                    for (Integer d: dependency.get(i1)) {

                        if(!clashList.contains(d))
                            clashList.add(d);

                    }
                    Collections.sort(clashList);
                    return true;
                }
            }
        }
        return false;

    }

}




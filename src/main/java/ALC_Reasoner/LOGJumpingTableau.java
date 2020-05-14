package ALC_Reasoner;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import java.util.*;
import java.util.stream.Collectors;

public class LOGJumpingTableau extends JumpingTableau{

    protected LOGJumpingTableau(OWLClassExpression concept, int parent) {

        super(concept,parent);
        LoggerManager.writeDebugLog("SAT: "+ parent, LOGJumpingTableau.class);

    }

    @Override
    protected boolean applyIntersection(OWLObjectIntersectionOf intersection){
        LoggerManager.writeDebugLog("Rule: " + workingRule + " INTERSECTION: "+ OntologyRenderer.render(intersection), LOGJumpingTableau.class);

        return super.applyIntersection(intersection);
    }

    @Override
    protected boolean applyUnion(OWLObjectUnionOf union){
        LoggerManager.writeDebugLog("Rule: "+ workingRule + " UNION: " + OntologyRenderer.render(union), LOGJumpingTableau.class);

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
                LoggerManager.writeDebugLog("CHOICE " + OntologyRenderer.render(owlClassExpression), LOGJumpingTableau.class);

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

                    LoggerManager.writeDebugLog("BACKTRACK: " + rule, LOGJumpingTableau.class);

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
        LoggerManager.writeDebugLog("Rule: " + workingRule + " SOME: " + OntologyRenderer.render(someValue), LOGJumpingTableau.class);

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
                    LoggerManager.writeDebugLog("SOME ALREADY PRESENT", LOGJumpingTableau.class);

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

        direct = new LOGJumpingTableau(filler, workingRule);
        if(direct.SAT()) {
            LoggerManager.writeDebugLog("SOME "+workingRule+" SATISFIABLE", LOGJumpingTableau.class);

            related.add(related.size(),workingRule);
            someRelation.put(oe, related);
            workingRule++;
            iteration += direct.getIteration();
            return SAT();

        }
        else{
            LoggerManager.writeDebugLog("SOME UNSATISFIABLE", LOGJumpingTableau.class);

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
        LoggerManager.writeDebugLog("Rule: " + workingRule + " ALL: "+ OntologyRenderer.render(allValue), LOGJumpingTableau.class);

        OWLClassExpression filler = allValue.getFiller();
        OWLObjectPropertyExpression oe = allValue.getProperty();

        if (someRelation.get(oe) == null){
            LoggerManager.writeDebugLog("ALL NO CONDITIONS", LOGJumpingTableau.class);

            iteration++;

        }
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
                    Tableau Tflag = new LOGJumpingTableau(concept, workingRule);

                    if (!Tflag.SAT()) {
                        LoggerManager.writeDebugLog("ALL UNSATISFIABLE", LOGJumpingTableau.class);

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
                    LoggerManager.writeDebugLog("ALL "+workingRule+" SATISFIABLE", ChronologicalTableau.class);


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
                    LoggerManager.writeDebugLog("CLASH "+ OntologyRenderer.render(c) + " | " +OntologyRenderer.render(c1), LOGJumpingTableau.class);
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




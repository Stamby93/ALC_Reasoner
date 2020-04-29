import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.ArrayList;
import java.util.List;

public class subNode extends Node {

    public subNode(List<OWLClassExpression> operand, int workingRule) {
        super(operand,workingRule);

    }

    public void alterRule(OWLClassExpression o, int rule){
        super.oldAbox.set(rule,o);
    }



    public void updateStatus(List<Integer> toDelete, int toUpdate, OWLClassExpression rule){

        oldAbox.set(toUpdate,rule);
        ArrayList<OWLClassExpression> newAbox = new ArrayList<>(oldAbox);
        int shiftWorkingRule = 0;
        for (Integer i = 0; i < toDelete.size() && toDelete.get(i) < oldAbox.size(); i++) {

            if(toDelete.get(i) < workingRule)
                shiftWorkingRule++;

            newAbox.remove(oldAbox.get(toDelete.get(i)));

        }
        workingRule-=shiftWorkingRule;
        oldAbox.removeAll(oldAbox);
        oldAbox.addAll(newAbox);

    }

}

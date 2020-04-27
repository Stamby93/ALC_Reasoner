import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.List;

/**
 * 
 */
public interface Tableau {

    public boolean SAT();

    public String getModel();
    public Integer getIteration();
    public List<OWLClassExpression> getAbox();
    public int getParent();

}
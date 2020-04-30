import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.List;

/**
 * 
 */
public interface Tableau {

    boolean SAT();

    String getModel();
    Integer getIteration();

}
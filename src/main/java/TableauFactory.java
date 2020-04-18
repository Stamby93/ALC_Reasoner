
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * 
 */
public class TableauFactory {

    /**
     * Default constructor
     */
    public TableauFactory() {
    }

    /**
     * @param type 
     * @param Concept 
     * @return
     */
    public Tableau getTableau(String type, OWLClassExpression Concept) {
        switch (type){
            case "Naive":
                return new NaiveTableau(Concept, null, -1);
        }
        return null;
    }

}
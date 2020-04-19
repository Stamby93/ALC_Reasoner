
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
     * @return Tableau
     */
    public Tableau getTableau(String type, OWLClassExpression Concept) {
        switch (type){
            case "Naive":
                return new NaiveTableau(Concept, -1);
        }
        return null;
    }

}
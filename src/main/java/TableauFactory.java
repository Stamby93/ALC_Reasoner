
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


    public Tableau getTableau(String type, OWLClassExpression Concept) {
        switch (type){
            case "Chronological":
                return new myTableau(Concept, -1);
            case "Jumping":
                return new myJumpingTableau(Concept, -1);
            default:
                return null;
        }
    }

}
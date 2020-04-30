
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
                return new ChronologicalTableau(Concept, -1);
            case "Jumping":
                return new JumpingTableau(Concept, -1);
            case "Dynamic":
                return null;//new DynamicTableau(Concept, -1);
            default:
                return null;
        }
    }

}
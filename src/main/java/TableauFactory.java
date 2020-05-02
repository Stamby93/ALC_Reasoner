
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * The Tableau factory.
 * Application of factory pattern, return different object, of type
 * Tableau, depending on the type specified.
 */
public class TableauFactory {

    /**
     * Default constructor
     */
    public TableauFactory() {
    }


    /**
     * Gets tableau.
     *
     * @param type    the type
     * @param Concept the concept
     * @return the tableau
     */
    public Tableau getTableau(String type, OWLClassExpression Concept) {
        return switch (type) {
            case "Chronological" -> new ChronologicalTableau(Concept, -1);
            case "Jumping" -> new JumpingTableau(Concept, -1);
            default -> null;
        };
    }

}
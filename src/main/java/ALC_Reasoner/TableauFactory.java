package ALC_Reasoner;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * The ALC_Reasoner.Tableau factory.
 * Application of factory pattern, return different object, of type
 * ALC_Reasoner.Tableau, depending on the type specified.
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
    Tableau getTableau(String type, OWLClassExpression Concept) {
        switch (type) {
            case "Chronological":
                return new ChronologicalTableau(Concept);
            case "Jumping":
                return new JumpingTableau(Concept);
            case "LOGChronological":
                return new LOGChronologicalTableau(Concept, -1);
            case "LOGJumping":
                return new LOGJumpingTableau(Concept, -1);

            default:
                return null;
        }
    }

}
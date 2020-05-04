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
                return new ChronologicalTableau(Concept, -1);
            case "Jumping":
                return new JumpingTableau(Concept, -1);
            default:
                return null;
        }
    }

}
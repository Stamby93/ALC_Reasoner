import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * The type ALC reasoner factory.
 */
public class ALCReasonerFactory implements OWLReasonerFactory {

    private String type = "Chronological";

    /**
     * Default constructor
     */
    public ALCReasonerFactory() {
    }

    /**
     * Instantiates a new Alc reasoner factory.
     *
     * @param type String Represents the reasoning strategy to be used.
     */
    public ALCReasonerFactory(String type){
        this.type = type;
    }

    /**
     * Create reasoner.
     *
     * @return OWLReasoner Reasoner for ALC
     */
    public OWLReasoner createReasoner() {
        return new ALCReasoner(type);
    }

    /**
     * @return OWLReasoner Reasoner for ALC
     */
    public OWLReasoner createReasoner(OWLOntology ontology) {
        if(ontology == null)
            return createReasoner();
        throw new RuntimeException("Input ontology must be empty");
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology owlOntology, OWLReasonerConfiguration owlReasonerConfiguration) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner factory");
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology owlOntology, OWLReasonerConfiguration owlReasonerConfiguration) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner factory");
    }

    @Override
    public String getReasonerName() {
        return "ALC Reasoner";
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology owlOntology) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner factory");
    }
}
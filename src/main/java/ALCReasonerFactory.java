import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * 
 */
public class ALCReasonerFactory implements OWLReasonerFactory {

    private String type = null; //"Chronological";
    /**
     * Default constructor
     */
    public ALCReasonerFactory() {
    }

    public ALCReasonerFactory(String type){
        this.type = type;
    }

    /**
     * @return OWLReasoner
     */
    public OWLReasoner createReasoner() {
        return new ALCReasoner(type);
    }

    /**
     * @return OWLReasoner
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
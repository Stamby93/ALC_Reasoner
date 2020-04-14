
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;

/**
 * 
 */
public class ALCReasonerFactory implements OWLReasonerFactory {

    /**
     * Default constructor
     */
    public ALCReasonerFactory() {
    }

    /**
     * @return
     */
    public ALCReasoner createReasoner() {
        // TODO implement here
        return null;
    }

    /**
     * @param ontology 
     * @return
     */
    public OWLReasoner createReasoner(OWLOntology ontology) {
        // TODO implement here
        return null;
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology owlOntology, OWLReasonerConfiguration owlReasonerConfiguration) {
        return null;
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology owlOntology, OWLReasonerConfiguration owlReasonerConfiguration) {
        return null;
    }

    /**
     * 
     */
    public void Operation2() {
        // TODO implement here
    }

    @Override
    public String getReasonerName() {
        return null;
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology owlOntology) {
        return null;
    }
}
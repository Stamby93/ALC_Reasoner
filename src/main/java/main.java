import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

public class main {

    public static void main(String[] args) throws Exception {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("prima.owl"));
        OWLDataFactory df = man.getOWLDataFactory();
        IRI iri = ont.getOntologyID().getOntologyIRI().get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");
        Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

        if(ontologyAxiom.size() > 1)
            throw new IllegalArgumentException("Invalid input concept");

        OWLEquivalentClassesAxiomImpl axiom = (OWLEquivalentClassesAxiomImpl) ontologyAxiom.iterator().next();

        Set<OWLClassExpression> expressions = axiom.classExpressions().collect(Collectors.toSet());
        OWLClassExpression expression = null;
        for (OWLClassExpression e: expressions) {
            if(e.isOWLClass() == false) {
                expression = e;
                break;
            }
        }

        System.out.println("Concetto in input: " + expression.toString());

        /*OWLReasonerFactory ReasonerFactory = new ALCReasonerFactory();
        OWLReasoner ALCReasoner = ReasonerFactory.createReasoner(null);

            System.out.println("The concept is" + ALCReasoner.isSatisfiable(expression));
        }
*/



    }

}

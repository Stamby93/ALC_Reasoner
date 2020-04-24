import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
public class Launcher {

    public static void main(String[] args) throws Exception {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File ontologyFile = new File("Ontologie/mpp.owl");
        OWLOntology ont = man.loadOntologyFromOntologyDocument(ontologyFile);
        OWLDataFactory df = man.getOWLDataFactory();
        IRI iri = ont.getOntologyID().getOntologyIRI().get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");
        Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

        LoggerManager.setFile(ontologyFile.getName());

        if (ontologyAxiom.size() > 1) {
            LoggerManager.writeErrorLog("Invalid input concept", Launcher.class);
            throw new IllegalArgumentException("Invalid input concept");
        }

        OWLEquivalentClassesAxiomImpl axiom = (OWLEquivalentClassesAxiomImpl) ontologyAxiom.iterator().next();

        Set<OWLClassExpression> expressions = axiom.classExpressions().collect(Collectors.toSet());
        OWLClassExpression expression = null;
        for (OWLClassExpression e : expressions) {
            if (!e.isOWLClass()) {
                expression = e;
                break;
            }
        }

        if(expression != null) {

            System.out.println("Concetto in input:");
            System.out.println(expression.toString());
            System.out.println("\nManchester Sintax:");
            System.out.println(OntologyRenderer.render(expression.getNNF()) + "\n");

            OWLReasonerFactory ReasonerFactory = new ALCReasonerFactory();
            OWLReasoner reasoner = ReasonerFactory.createReasoner(null);
            boolean result = reasoner.isSatisfiable(expression.getNNF());
            System.out.println("\n\nThe concept is " + result);
            LoggerManager.writeInfoLog("The concept is " + result, Launcher.class);
            if(result) {
                String model = "Modello trovato: |"+((ALCReasoner)reasoner).getModel();
                System.out.println(model);
                LoggerManager.writeInfoLog(model, Launcher.class);
            }
        }
    }

}

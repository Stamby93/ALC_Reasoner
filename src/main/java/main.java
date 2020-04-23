import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
public class main {

    public static void main(String[] args) throws Exception {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File ontologyFile = new File("Ontologie/18-02-08_PROVA.owl");
        OWLOntology ont = man.loadOntologyFromOntologyDocument(ontologyFile);
        OWLDataFactory df = man.getOWLDataFactory();
        IRI iri = ont.getOntologyID().getOntologyIRI().get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");
        Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

        LoggerManager.setFile(ontologyFile.getName());

        if (ontologyAxiom.size() > 1) {
            LoggerManager.writeErrorLog("Invalid input concept", main.class);
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
            OWLReasoner ALCReasoner = ReasonerFactory.createReasoner(null);
            boolean result = ALCReasoner.isSatisfiable(expression.getNNF());
            System.out.println("\n\nThe concept is " + result);
            LoggerManager.writeInfoLog("The concept is " + result,main.class);
        }
    }

}

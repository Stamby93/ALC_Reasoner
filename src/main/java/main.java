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
        OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("Ontologie/18-02-08_PROVA.owl"));
        OWLDataFactory df = man.getOWLDataFactory();
        IRI iri = ont.getOntologyID().getOntologyIRI().get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");
        Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

        if (ontologyAxiom.size() > 1)
            throw new IllegalArgumentException("Invalid input concept");

        OWLEquivalentClassesAxiomImpl axiom = (OWLEquivalentClassesAxiomImpl) ontologyAxiom.iterator().next();

        Set<OWLClassExpression> expressions = axiom.classExpressions().collect(Collectors.toSet());
        OWLClassExpression expression = null;
        for (OWLClassExpression e : expressions) {
            if (!e.isOWLClass()) {
                expression = e;
                break;
            }
        }

        assert expression != null;
        ShortFormProvider shortFormProvider = new
                SimpleShortFormProvider();
        OWLObjectRenderer renderer = new
                ManchesterOWLSyntaxOWLObjectRendererImpl();
        renderer.setShortFormProvider(shortFormProvider);
        System.out.println("Concetto in input:");
        System.out.println(expression.toString());
        System.out.println("\nManchester Sintax:");
        System.out.println(renderer.render(expression)+"\n");

        OWLReasonerFactory ReasonerFactory = new ALCReasonerFactory();
        OWLReasoner ALCReasoner;
        ALCReasoner = ReasonerFactory.createReasoner(null);
        System.out.println("\n\nThe concept is "+ALCReasoner.isSatisfiable(expression.getNNF()));

    }

}


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.HermiT.ReasonerFactory;
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

        ReasonerFactory factoryHermit = new ReasonerFactory();

        OWLReasoner oracle = factoryHermit.createReasoner(ont);

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
            long ALC_StartTime = System.currentTimeMillis();
            boolean result = reasoner.isSatisfiable(expression.getNNF());
            long ALC_EndTime = System.currentTimeMillis();
            System.out.println("\n\nThe ALC concept is " + result + " ("+(ALC_EndTime - ALC_StartTime) + " milliseconds)");

            long HERMIT_StartTime = System.currentTimeMillis();
            boolean Hresult = oracle.isSatisfiable(expression.getNNF());
            long HERMIT_EndTime = System.currentTimeMillis();
            System.out.println("\nThe Hermit concept is " + Hresult + " ("+(HERMIT_EndTime - HERMIT_StartTime) + " milliseconds)");

            LoggerManager.writeInfoLog("The concept is " + result, Launcher.class);
            LoggerManager.writeInfoLog("The Hermit concept is " + Hresult, Launcher.class);

            if(result) {
                String model = "\nModello trovato: |"+((ALCReasoner)reasoner).getModel();
                System.out.println(model);
                LoggerManager.writeInfoLog(model, Launcher.class);
            }
        }
    }

}

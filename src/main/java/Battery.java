import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public class Battery {

    public static void main(String[] args) throws Exception {

        File dir = new File("Ontologie");
        File[] directoryListing = dir.listFiles();
        assert directoryListing != null;
        for (File ontologyFile : directoryListing) {

            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLOntology ont = man.loadOntologyFromOntologyDocument(ontologyFile);
            OWLDataFactory df = man.getOWLDataFactory();
            IRI iri = ont.getOntologyID().getOntologyIRI().get();
            OWLClass flag = df.getOWLClass(iri + "#assioma");
            Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

            /*TABLEAU Chronological*/
            OWLReasonerFactory factoryALC_chrono = new ALCReasonerFactory();
            OWLReasoner alc_chrono = factoryALC_chrono.createReasoner(null);

            /*TABLEAU Jumping*/
            OWLReasonerFactory factoryALC_jump = new ALCReasonerFactory("Jumping");
            OWLReasoner alc_jump = factoryALC_jump.createReasoner(null);

            LoggerManager.setFile(ontologyFile.getName());
            System.out.println("\n"+ontologyFile.getName());

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

            if (expression != null) {

                LoggerManager.writeInfoLog("\n\n\nSTARTING Chronological Tableau\n\n\n", Launcher.class);
                long chrono_StartTime = System.currentTimeMillis();
                boolean resultChrono = alc_chrono.isSatisfiable(expression);
                long chrono_EndTime = System.currentTimeMillis();
                Integer chronoIteration = ((ALCReasoner) alc_chrono).getIteration();
                System.out.println("ALC(Chronological Tableau): " + resultChrono + " (" + (chrono_EndTime - chrono_StartTime) + " milliseconds - " + chronoIteration + " iterazioni");
                LoggerManager.writeInfoLog("ALC(Chronological Tableau): " + resultChrono, Launcher.class);

                LoggerManager.writeInfoLog("\n\n\nSTARTING Jumping Tableau\n\n\n", Launcher.class);

                long jump_StartTime = System.currentTimeMillis();
                boolean resultJump = alc_jump.isSatisfiable(expression);
                long jump_EndTime = System.currentTimeMillis();
                Integer jumpIteration = ((ALCReasoner) alc_jump).getIteration();
                System.out.println("ALC(Jumping Tableau): " + resultJump + " (" + (jump_EndTime - jump_StartTime) + " milliseconds) - " + jumpIteration + " iterazioni");
                LoggerManager.writeInfoLog("ALC(Jumping Tableau): " + resultJump, Launcher.class);
            }
        }
    }

}

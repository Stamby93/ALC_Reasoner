import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Battery {

    public static void main(String[] args) throws Exception {
        final boolean DEBUG = true;
        File dir = new File("Ontologie");
        File[] directoryListing = dir.listFiles();
        assert directoryListing != null;
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        for (File ontologyFile : directoryListing) {

            man.clearOntologies();
            OWLOntology ont = man.loadOntologyFromOntologyDocument(ontologyFile);
            OWLDataFactory df = man.getOWLDataFactory();
            Optional<IRI> optIri = ont.getOntologyID().getOntologyIRI();
            assert optIri.isPresent();
            IRI iri = optIri.get();
            OWLClass flag = df.getOWLClass(iri + "#assioma");
            Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

            /*TABLEAU Chronological*/
            OWLReasonerFactory factoryALC_chrono = new ALCReasonerFactory();
            OWLReasoner alc_chrono = factoryALC_chrono.createReasoner(null);

            /*TABLEAU Jumping*/
            OWLReasonerFactory factoryALC_jump = new ALCReasonerFactory("Jumping");
            OWLReasoner alc_jump = factoryALC_jump.createReasoner(null);

            if (ontologyAxiom.size() > 1) {
                LoggerManager.setFile(ontologyFile.getName().replace(".owl",""), Battery.class);
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

                /*ChronologicalTableau*/
                if (DEBUG) {
                    LoggerManager.setFile(ontologyFile.getName().replace(".owl", "") + "_Chronological", Battery.class);
                }
                System.out.println("\n"+ontologyFile);
                long chrono_StartTime = System.currentTimeMillis();
                boolean resultChrono = alc_chrono.isSatisfiable(expression);
                long chrono_EndTime = System.currentTimeMillis();
                Integer chronoIteration = ((ALCReasoner) alc_chrono).getIteration();
                String chrono_model = ((ALCReasoner) alc_chrono).getModel();

                System.out.println("ALC(Chronological Tableau): " + resultChrono + " ("+(chrono_EndTime - chrono_StartTime) + " milliseconds) - ("+chronoIteration+" iterations)");

                LoggerManager.writeInfoLog("ALC(Chronological Tableau): " + resultChrono, Battery.class);
                LoggerManager.writeInfoLog("Iterazioni: " + chronoIteration, Battery.class);
                if (resultChrono)
                    LoggerManager.writeInfoLog("Model: " + chrono_model, Battery.class);


                /*JumpingTableau*/
                if (DEBUG) {
                    LoggerManager.setFile(ontologyFile.getName().replace(".owl", "") + "_Jumping", Battery.class);
                }
                long jump_StartTime = System.currentTimeMillis();
                boolean resultJump = alc_jump.isSatisfiable(expression);
                long jump_EndTime = System.currentTimeMillis();
                String jump_model = ((ALCReasoner) alc_jump).getModel();
                Integer jumpIteration = ((ALCReasoner) alc_jump).getIteration();
                System.out.println("ALC(Jumping Tableau): " + resultJump + " ("+(jump_EndTime - jump_StartTime) + " milliseconds) - ("+jumpIteration+" iterations)");

                LoggerManager.writeInfoLog("ALC(Jumping Tableau): " + resultJump, Battery.class);
                LoggerManager.writeInfoLog("Iterazioni: " + jumpIteration, Battery.class);
                if (resultJump)
                    LoggerManager.writeInfoLog("Model: " + jump_model, Battery.class);
            }
        }
    }

}

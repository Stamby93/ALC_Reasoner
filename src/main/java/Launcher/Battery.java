package Launcher;

import ALC_Reasoner.ALCReasoner;
import ALC_Reasoner.ALCReasonerFactory;
import ALC_Reasoner.LoggerManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Launcher.Battery.
 */
public class Battery {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        File dir = new File("Ontologie");
        File[] directoryListing = dir.listFiles();
        assert directoryListing != null;
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        LoggerManager.setFile("Result", Battery.class, true);
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
            OWLReasonerFactory factoryALC_chrono = new ALCReasonerFactory("LOGChronological");
            OWLReasoner alc_chrono = factoryALC_chrono.createReasoner(null);

            /*TABLEAU Jumping*/
            OWLReasonerFactory factoryALC_jump = new ALCReasonerFactory("LOGJumping");
            OWLReasoner alc_jump = factoryALC_jump.createReasoner(null);

            if (ontologyAxiom.size() > 1) {
                LoggerManager.setFile(ontologyFile.getName().replace(".owl",""), Battery.class, true);
                LoggerManager.writeErrorLog("Invalid input concept", Battery.class);
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

                /*ALC_Reasoner.ChronologicalTableau*/

                LoggerManager.setFile(ontologyFile.getName().replace(".owl", "") + "_Chronological", Battery.class, true);

                System.out.println(ontologyFile);
                long chrono_StartTime = System.currentTimeMillis();
                boolean resultChrono = alc_chrono.isSatisfiable(expression);
                long chrono_EndTime = System.currentTimeMillis();
                Integer chronoIteration = ((ALCReasoner) alc_chrono).getIteration();
                String chrono_model = ((ALCReasoner) alc_chrono).getModel();
                String chronoTot = "ALC(ChronologicalTableau): " + resultChrono + " ("+(chrono_EndTime - chrono_StartTime) + " milliseconds) - ("+chronoIteration+" iterations)";

                System.out.println(chronoTot);

                LoggerManager.writeInfoLog("ALC(ChronologicalTableau): " + resultChrono, Battery.class);
                LoggerManager.writeInfoLog("Iterations: " + chronoIteration, Battery.class);
                if (resultChrono)
                    LoggerManager.writeInfoLog("Model: " + chrono_model, Battery.class);


                /*ALC_Reasoner.JumpingTableau*/

                LoggerManager.setFile(ontologyFile.getName().replace(".owl", "") + "_Jumping", Battery.class, true);

                long jump_StartTime = System.currentTimeMillis();
                boolean resultJump = alc_jump.isSatisfiable(expression);
                long jump_EndTime = System.currentTimeMillis();
                String jump_model = ((ALCReasoner) alc_jump).getModel();
                Integer jumpIteration = ((ALCReasoner) alc_jump).getIteration();

                String jumpTot = "ALC(JumpingTableau): " + resultJump + " ("+(jump_EndTime - jump_StartTime) + " milliseconds) - ("+jumpIteration+" iterations)";
                System.out.println(jumpTot+"\n");

                LoggerManager.writeInfoLog("ALC(JumpingTableau): " + resultJump, Battery.class);
                LoggerManager.writeInfoLog("Iterations: " + jumpIteration, Battery.class);
                if (resultJump)
                    LoggerManager.writeInfoLog("Model: " + jump_model, Battery.class);

                LoggerManager.setFile("Result", Battery.class, false);
                LoggerManager.writeInfoLog(ontologyFile.getName(), Battery.class);
                LoggerManager.writeInfoLog(chronoTot, Battery.class);
                if (resultChrono)
                    LoggerManager.writeInfoLog("Model: " + chrono_model, Battery.class);
                LoggerManager.writeInfoLog(jumpTot, Battery.class);
                if (resultJump)
                    LoggerManager.writeInfoLog("Model: " + jump_model, Battery.class);

            }
        }
    }

}

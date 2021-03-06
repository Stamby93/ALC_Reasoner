package Launcher;

import ALC_Reasoner.ALCReasoner;
import ALC_Reasoner.ALCReasonerFactory;
import ALC_Reasoner.LoggerManager;
import ALC_Reasoner.OntologyRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Launcher.Launcher.
 */
public class Launcher {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File ontologyFile = new File("Ontologie/09-06-01.owl");
        OWLOntology ont = man.loadOntologyFromOntologyDocument(ontologyFile);
        OWLDataFactory df = man.getOWLDataFactory();
        Optional<IRI> optIri = ont.getOntologyID().getOntologyIRI();
        assert optIri.isPresent();
        IRI iri = optIri.get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");
        Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

        /*HERMIT*/
        ReasonerFactory factoryHermit = new ReasonerFactory();
        OWLReasoner hermit = factoryHermit.createReasoner(ont);

        /*TABLEAU Chronological*/
        OWLReasonerFactory factoryALC_chrono = new ALCReasonerFactory();
        OWLReasoner alc_chrono = factoryALC_chrono.createReasoner(null);

        /*TABLEAU Jumping*/
        OWLReasonerFactory factoryALC_jump = new ALCReasonerFactory("Jumping");
        OWLReasoner alc_jump = factoryALC_jump.createReasoner(null);

        LoggerManager.setFile(ontologyFile.getName().replace(".owl", ""), Launcher.class,true);

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
            System.out.println(OntologyRenderer.render(expression) + "\n");

            /*HermiT*/
            long hermit_StartTime = System.currentTimeMillis();
            boolean resultHermit = hermit.isSatisfiable(expression);
            long hermit_EndTime = System.currentTimeMillis();
            System.out.println("HermiT: " + resultHermit + " ("+(hermit_EndTime - hermit_StartTime) + " milliseconds)");
            LoggerManager.writeInfoLog("\nHermiT: " + resultHermit, Launcher.class);

            /*ChronologicaTableau*/
            LoggerManager.setFile(ontologyFile.getName().replace(".owl", "") + "_Chronological", Launcher.class,true);
            long chrono_StartTime = System.currentTimeMillis();
            boolean resultChrono = alc_chrono.isSatisfiable(expression);
            long chrono_EndTime = System.currentTimeMillis();
            Integer chronoIteration = ((ALCReasoner) alc_chrono).getIteration();
            System.out.println("\nALC(Chronological ALC_Reasoner.Tableau): " + resultChrono + " ("+(chrono_EndTime - chrono_StartTime) + " milliseconds) - ("+chronoIteration+" iterations)");
            LoggerManager.writeInfoLog("ALC(Chronological ALC_Reasoner.Tableau): " + resultChrono, Launcher.class);
            if(resultChrono) {
                String model = "Modello trovato: "+((ALCReasoner)alc_chrono).getModel();
                LoggerManager.writeInfoLog(model, Launcher.class);
            }
            /*ALC_Reasoner.JumpingTableau*/

            LoggerManager.setFile(ontologyFile.getName().replace(".owl", "") + "_Jumping", Launcher.class,true);
            long jump_StartTime = System.currentTimeMillis();
            boolean resultJump = alc_jump.isSatisfiable(expression);
            long jump_EndTime = System.currentTimeMillis();
            Integer jumpIteration = ((ALCReasoner) alc_jump).getIteration();
            System.out.println("ALC(Jumping ALC_Reasoner.Tableau): " + resultJump + " ("+(jump_EndTime - jump_StartTime) + " milliseconds) - ("+jumpIteration+" iterations)");
            LoggerManager.writeInfoLog("ALC(Jumping ALC_Reasoner.Tableau): " + resultJump, Launcher.class);
            if(resultJump) {
                String model = "Modello trovato: "+((ALCReasoner)alc_jump).getModel();
                LoggerManager.writeInfoLog(model, Launcher.class);
            }

            /*DynamicTableau*/
        }
    }


}

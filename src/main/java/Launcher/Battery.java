package Launcher;

import ALC_Reasoner.ALCReasoner;
import ALC_Reasoner.ALCReasonerFactory;
import ALC_Reasoner.LoggerManager;
import ALC_Reasoner.OntologyRenderer;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Launcher.Battery.
 * This class was implemented with the aim of making a direct comparison
 * between the performances of the Chronological and those of the Jumping Tableau
 */
public class Battery {
    private final ReasonerFactory factoryHermit;
    private final File[] directoryListing;
    private final OWLOntologyManager man;
    OWLReasonerFactory factoryALC_chrono;
    OWLReasonerFactory factoryALC_jump;
    OWLReasoner alc_chrono;
    OWLReasoner alc_jump;



    public Battery(){
        File dir = new File("Ontologie");
        directoryListing = dir.listFiles();
        man = OWLManager.createOWLOntologyManager();
        factoryHermit = new ReasonerFactory();
    }

    /**
     *
     * @param log Boolean value to set LOG modality
     * @throws Exception if problem occurs
     * @return String that contains the output
     */
    public String start(boolean log) throws Exception {

        String output = "************************************************************************************";
        assert directoryListing != null;

        if(log)
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
            if(log)
                factoryALC_chrono = new ALCReasonerFactory("LOGChronological");
            else
                factoryALC_chrono = new ALCReasonerFactory("Chronological");
            alc_chrono = factoryALC_chrono.createReasoner(null);

            /*TABLEAU Jumping*/
            if(log)
                factoryALC_jump = new ALCReasonerFactory("LOGJumping");
            else
                factoryALC_jump = new ALCReasonerFactory("Jumping");

            alc_jump = factoryALC_jump.createReasoner(null);

            if (ontologyAxiom.size() > 1) {

                if(log){

                    LoggerManager.setFile(ontologyFile.getName().replace(".owl",""), Battery.class, true);
                    LoggerManager.writeErrorLog("Invalid input concept", Battery.class);

                }
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

                output = output.concat("\n"+ontologyFile.getName()+"\n\n");

                /*ALC_Reasoner.ChronologicalTableau*/
                if(log)
                    LoggerManager.setFile(ontologyFile.getName().replace(".owl", "") + "_Chronological", Battery.class, true);

                //System.out.println(ontologyFile);
                long chrono_StartTime = System.currentTimeMillis();
                boolean resultChrono = alc_chrono.isSatisfiable(expression);
                long chrono_EndTime = System.currentTimeMillis();
                Integer chronoIteration = ((ALCReasoner) alc_chrono).getIteration();
                String chrono_model = ((ALCReasoner) alc_chrono).getModel();
                String chronoTot = "ALC(ChronologicalTableau): " + resultChrono + " ("+(chrono_EndTime - chrono_StartTime) + " milliseconds) - ("+chronoIteration+" iterations)";

                //System.out.println(chronoTot);

                output = output.concat(chronoTot+"\n");
                if (resultChrono)
                    output = output.concat("Model: " + chrono_model+"\n");


                if(log) {
                    LoggerManager.writeInfoLog("ALC(ChronologicalTableau): " + resultChrono, Battery.class);
                    LoggerManager.writeInfoLog("Iterations: " + chronoIteration, Battery.class);
                    if (resultChrono)
                        LoggerManager.writeInfoLog("Model: " + chrono_model, Battery.class);
                }


                /*ALC_Reasoner.JumpingTableau*/
                if(log)
                    LoggerManager.setFile(ontologyFile.getName().replace(".owl", "") + "_Jumping", Battery.class, true);

                long jump_StartTime = System.currentTimeMillis();
                boolean resultJump = alc_jump.isSatisfiable(expression);
                long jump_EndTime = System.currentTimeMillis();
                String jump_model = ((ALCReasoner) alc_jump).getModel();
                Integer jumpIteration = ((ALCReasoner) alc_jump).getIteration();

                String jumpTot = "ALC(JumpingTableau): " + resultJump + " ("+(jump_EndTime - jump_StartTime) + " milliseconds) - ("+jumpIteration+" iterations)";
                //System.out.println(jumpTot+"\n");

                output = output.concat("\n"+jumpTot+"\n");
                if (resultChrono)
                    output = output.concat("Model: " + jump_model+ "\n");

                if(log){
                    LoggerManager.writeInfoLog("ALC(JumpingTableau): " + resultJump, Battery.class);
                    LoggerManager.writeInfoLog("Iterations: " + jumpIteration, Battery.class);

                    if (resultJump )
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

                OWLReasoner oracle = factoryHermit.createReasoner(ont);
                /*HermiT*/
                long hermit_StartTime = System.currentTimeMillis();
                boolean resultHermit = oracle.isSatisfiable(expression);
                long hermit_EndTime = System.currentTimeMillis();
                output = output.concat("\nHermiT: " + resultHermit + " (" + (hermit_EndTime - hermit_StartTime) + " milliseconds)\n");

                if(!(resultChrono==resultHermit && resultJump==resultHermit))
                    throw new RuntimeException("Test Failed with file: "+ ontologyFile.getName());
            }
            output = output.concat("\n************************************************************************************");
        }
        return output;
    }

    public String single(File file, boolean log) throws Exception{
        
        String output = "";
        OWLClassExpression expression = null;

        man.clearOntologies();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(file);

        OWLDataFactory df = man.getOWLDataFactory();
        assert ont != null;
        Optional<IRI> optIri = ont.getOntologyID().getOntologyIRI();
        assert optIri.isPresent();
        IRI iri = optIri.get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");

        Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());
        /*HERMIT*/
        ReasonerFactory factoryHermit = new ReasonerFactory();
        OWLReasoner hermit = factoryHermit.createReasoner(ont);

        /*TABLEAU Chronological*/
        if(log)
            factoryALC_chrono = new ALCReasonerFactory("LOGChronological");
        else
            factoryALC_chrono = new ALCReasonerFactory("Chronological");
        alc_chrono = factoryALC_chrono.createReasoner(null);

        /*TABLEAU Jumping*/
        if(log)
            factoryALC_jump = new ALCReasonerFactory("LOGJumping");
        else
            factoryALC_jump = new ALCReasonerFactory("Jumping");

        alc_jump = factoryALC_jump.createReasoner(null);

        /*Logger*/
        if(log)
            LoggerManager.setFile(file.getName().replace(".owl", ""), LauncherGUI.class, true);


        if (ontologyAxiom.size() > 1) {
            if(log)
                LoggerManager.writeErrorLog("Invalid input concept", LauncherGUI.class);
            throw new IllegalArgumentException("Invalid input concept");
        }

        OWLEquivalentClassesAxiomImpl axiom = (OWLEquivalentClassesAxiomImpl) ontologyAxiom.iterator().next();

        Set<OWLClassExpression> expressions = axiom.classExpressions().collect(Collectors.toSet());
        for (OWLClassExpression E : expressions) {
            if (!E.isOWLClass()) {
                expression = E;
                break;
            }
        }

        if (expression != null) {
            
            output = output.concat("\nOpening: " + file.getName() + "." + "\n");
            OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
            renderer.setShortFormProvider(new SimpleShortFormProvider());
            output = output.concat("\nConcetto in input: " + renderer.render(expression) + "." + "\n");
            output = output.concat("\nManchester Sintax: \n\n" + OntologyRenderer.render(expression) + "." + "\n");
            output = output.concat("\n---------------- CHECK CONCEPT ----------------" + "\n");

            /*ChronologicaTableau*/
            if(log)
                LoggerManager.setFile(file.getName().replace(".owl", "") + "_Chronological", LauncherGUI.class, true);
            long chrono_StartTime = System.currentTimeMillis();
            boolean resultChrono = alc_chrono.isSatisfiable(expression);
            long chrono_EndTime = System.currentTimeMillis();
            Integer chronoIteration=((ALCReasoner)alc_chrono).getIteration();

            output = output.concat("\nALC (Chronological ALC_Reasoner): " + resultChrono + " (" + (chrono_EndTime - chrono_StartTime) + " milliseconds) - ("+ chronoIteration + " iterations)" + "\n");
            if(log)
                LoggerManager.writeInfoLog("ALC (Chronological ALC_Reasoner): " + resultChrono, LauncherGUI.class);
            if(resultChrono) {
                String model = "Modello trovato: "+((ALCReasoner)alc_chrono).getModel()+ "\n";
                if(log)
                    LoggerManager.writeInfoLog(model, LauncherGUI.class);
                output = output.concat(model);
            }
            /*ALC_Reasoner.JumpingTableau*/
            if(log)
                LoggerManager.setFile(file.getName().replace(".owl", "") + "_Jumping", LauncherGUI.class, true);
            long jump_StartTime = System.currentTimeMillis();
            boolean resultJump = alc_jump.isSatisfiable(expression);
            long jump_EndTime = System.currentTimeMillis();
            Integer jumpIteration=((ALCReasoner)alc_jump).getIteration();
            output = output.concat("\nALC (Jumping ALC_Reasoner): " + resultJump + " (" + (jump_EndTime - jump_StartTime) + " milliseconds) - ("+ jumpIteration + " iterations)" + "\n");
            if(log)
                LoggerManager.writeInfoLog("ALC(Jumping ALC_Reasoner): " + resultJump, LauncherGUI.class);
            if(resultJump) {
                String model = "Modello trovato: "+((ALCReasoner)alc_jump).getModel()+ "\n";
                if(log)
                    LoggerManager.writeInfoLog(model, LauncherGUI.class);
                output = output.concat(model);
            }

            /*HermiT*/
            long hermit_StartTime = System.currentTimeMillis();
            boolean resultHermit = hermit.isSatisfiable(expression);
            long hermit_EndTime = System.currentTimeMillis();
            output = output.concat("\nHermiT: " + resultHermit + " (" + (hermit_EndTime - hermit_StartTime) + " milliseconds)" + "\n");

        }

        return output;
    }
}

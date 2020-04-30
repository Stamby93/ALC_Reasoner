import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class TestLauncher {
    private final boolean DEBUG = true;


    private OWLOntologyManager man;
    private String ontologyFileName;
    private boolean result;
    private boolean expResult;
    private OWLReasoner chronoReasoner;
    private OWLReasoner jumpReasoner;
    private OWLReasoner oracle;
    private OWLClassExpression expression;
    private ReasonerFactory factoryHermit;


    @Before
    public void setUp() {
        man = OWLManager.createOWLOntologyManager();
        OWLReasonerFactory chronoFactory = new ALCReasonerFactory("Chronological");
        OWLReasonerFactory jumpFactory = new ALCReasonerFactory("Jumping");

        chronoReasoner = chronoFactory.createReasoner(null);
        jumpReasoner = jumpFactory.createReasoner(null);

        factoryHermit = new ReasonerFactory();
    }

    @Test
    public void testOntology_Chronological_1() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/06-06-26.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }

    @Test
    public void testOntology_Jumping_1() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/06-06-26.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_2() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/06-07-24.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_2() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/06-07-24.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_3() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/09-06-01.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_3() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/09-06-01.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_4() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/12-06-08.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_4() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/12-06-08.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_5() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/17-01-27.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_5() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/17-01-27.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_6() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/17-02-23.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_6() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/17-02-23.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_7() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA_2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_7() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA_2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_8() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA_3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_8() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA_3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_9() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_9() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_10() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_10() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_11() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/AnotA.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_11() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/AnotA.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_12() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/Bottom.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_12() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/Bottom.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_13() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/enzo_Fantasy.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_13() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/enzo_Fantasy.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_14() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PerOgni.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_14() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PerOgni.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_15() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mostro.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_15() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mostro.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_16() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mostropiugrosso.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_16() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mostropiugrosso.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_17() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL1.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_17() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL1.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_18() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_18() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_19() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_19() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_20() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_20() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_21() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mgp.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_21() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mgp.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_22() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/Satisfiable.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_22() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/Satisfiable.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_23() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/stupido.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_23() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/stupido.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_24() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/stupido2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_24() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/stupido2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_25() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/spaccatuttoVero.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_25() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/spaccatuttoVero.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_26() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/spaccatuttoFalso.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_26() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/spaccatuttoFalso.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_27() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL1.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_27() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL1.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Chronological_28() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_28() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }

    @Test
    public void testOntology_Chronological_29() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Chronological(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }
    @Test
    public void testOntology_Jumping_29() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner_Jumping(expression);
        expResult = Hermit_Reasoner(expression);
        assertEquals(expResult, result);
    }

    private void loadOntology(String ontologyFileName) throws OWLOntologyCreationException {
        File ontologyFile = new File(ontologyFileName);
        OWLOntology ont = man.loadOntologyFromOntologyDocument(ontologyFile);
        if(DEBUG) {
            LoggerManager.setFile(ontologyFile.getName(), TestLauncher.class);
        }
        OWLDataFactory df = man.getOWLDataFactory();
        Optional<IRI> optIri = ont.getOntologyID().getOntologyIRI();
        assert optIri.isPresent();
        IRI iri = optIri.get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");
        Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());
        oracle = factoryHermit.createReasoner(ont);


        if (ontologyAxiom.size() > 1) {
            LoggerManager.writeErrorLog("Invalid input concept", Launcher.class);
            throw new IllegalArgumentException("Invalid input concept");
        }

        OWLEquivalentClassesAxiomImpl axiom = (OWLEquivalentClassesAxiomImpl) ontologyAxiom.iterator().next();

        Set<OWLClassExpression> expressions = axiom.classExpressions().collect(Collectors.toSet());
        expression = null;
        for (OWLClassExpression e : expressions) {
            if (!e.isOWLClass()) {
                expression = e;
                break;
            }
        }
    }

    private boolean Hermit_Reasoner(OWLClassExpression expression) {
        boolean result = false;
        if (expression != null) {
            result = oracle.isSatisfiable(expression);
        }
        return result;
    }

    private boolean ALC_Reasoner_Chronological(OWLClassExpression expression){
        boolean result = false;
        if (expression != null) {
            result = chronoReasoner.isSatisfiable(expression);
            LoggerManager.writeInfoLog("The concept is "+result, TestLauncher.class);
            if(result) {
                String model = "Modello trovato: |"+((ALCReasoner)chronoReasoner).getModel();
                LoggerManager.writeInfoLog(model, TestLauncher.class);
            }

        }
        return result;
    }

    private boolean ALC_Reasoner_Jumping(OWLClassExpression expression){
        boolean result = false;
        if (expression != null) {
            result = jumpReasoner.isSatisfiable(expression);
            LoggerManager.writeInfoLog("The concept is "+result, TestLauncher.class);
            if(result) {
                String model = "Modello trovato: |"+((ALCReasoner)jumpReasoner).getModel();
                LoggerManager.writeInfoLog(model, TestLauncher.class);
            }


        }
        return result;
    }

}

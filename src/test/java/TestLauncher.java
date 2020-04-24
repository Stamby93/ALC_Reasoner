import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;


public class TestLauncher {
    private OWLOntologyManager man;
    private String ontologyFileName;
    private boolean result;
    private boolean expResult;
    private OWLReasoner reasoner;
    @Before
    public void setUp() {
        man = OWLManager.createOWLOntologyManager();
        OWLReasonerFactory reasonerFactory = new ALCReasonerFactory();
        reasoner = reasonerFactory.createReasoner(null);
    }

    @Test
    public void testOntology1() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/06-06-26.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology2() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/06-07-24.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology3() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/09-06-01.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology4() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/12-06-08.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology5() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/17-01-27.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology6() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/17-02-23.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology7() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA_2.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology8() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA_3.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology9() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology10() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology11() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/AnotA.owl";
        result = run(ontologyFileName);
        expResult = false;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology12() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/Bottom.owl";
        result = run(ontologyFileName);
        expResult = false;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology13() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/enzo_Fantasy.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology14() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PerOgni.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology15() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mostro.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology16() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mostropiugrosso.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology17() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL1.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology18() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology19() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp2.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology20() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp3.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology21() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mgp.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology22() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/Satisfiable.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology23() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/stupido.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology24() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/stupido2.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology25() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/spaccatuttoVero.owl";
        result = run(ontologyFileName);
        expResult = true;
        assertEquals(result, expResult);
    }
    @Test
    public void testOntology26() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/spaccatuttoFalso.owl";
        result = run(ontologyFileName);
        expResult = false;
        assertEquals(result, expResult);
    }


    private boolean run(String ontologyFileName) throws OWLOntologyCreationException {
        File ontologyFile = new File(ontologyFileName);
        OWLOntology ont = man.loadOntologyFromOntologyDocument(ontologyFile);
        OWLDataFactory df = man.getOWLDataFactory();
        IRI iri = ont.getOntologyID().getOntologyIRI().get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");
        Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());
        boolean result = false;
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

        if (expression != null) {
            result = reasoner.isSatisfiable(expression.getNNF());
            if(result) {
                LoggerManager.writeInfoLog("The concept is "+result, TestLauncher.class);
                String model = "Modello trovato: |"+((ALCReasoner)reasoner).getModel();
                LoggerManager.writeInfoLog(model, TestLauncher.class);
            }
        }
        return result;
    }

}

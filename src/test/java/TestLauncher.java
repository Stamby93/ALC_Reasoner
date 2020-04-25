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
import java.util.Set;
import java.util.stream.Collectors;


public class TestLauncher {
    private OWLOntologyManager man;
    private String ontologyFileName;
    private boolean result;
    private boolean expResult;
    private OWLReasoner reasoner;
    private OWLReasoner oracle;
    private OWLOntology ont;
    private OWLClassExpression expression;
    private ReasonerFactory factoryHermit;


    @Before
    public void setUp() {
        man = OWLManager.createOWLOntologyManager();
        OWLReasonerFactory reasonerFactory = new ALCReasonerFactory();
        reasoner = reasonerFactory.createReasoner(null);
        factoryHermit = new ReasonerFactory();
    }

    @Test
    public void testOntology1() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/06-06-26.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        System.out.println("\n\nThe concept is " + result +" - The Hermit concept is "+ expResult);
        assertEquals(expResult, result);
}
    @Test
    public void testOntology2() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/06-07-24.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology3() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/09-06-01.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();

        assertEquals(expResult, result);
}
    @Test
    public void testOntology4() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/12-06-08.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology5() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/17-01-27.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology6() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/17-02-23.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology7() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA_2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology8() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA_3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology9() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08_PROVA.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology10() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/18-02-08.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology11() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/AnotA.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology12() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/Bottom.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology13() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/enzo_Fantasy.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology14() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PerOgni.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology15() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mostro.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology16() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mostropiugrosso.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology17() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL1.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology18() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology19() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology20() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mpp3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology21() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/mgp.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology22() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/Satisfiable.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology23() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/stupido.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology24() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/stupido2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology25() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/spaccatuttoVero.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}
    @Test
    public void testOntology26() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/spaccatuttoFalso.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}

    @Test
    public void testOntology27() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL1.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}

    @Test
    public void testOntology28() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL2.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}

    @Test
    public void testOntology29() throws OWLOntologyCreationException {
        ontologyFileName = "Ontologie/PROVASCAL3.owl";
        loadOntology(ontologyFileName);
        result = ALC_Reasoner();
        expResult = Hermit();
        assertEquals(expResult, result);
}

    private void loadOntology(String ontologyFileName) throws OWLOntologyCreationException {
        File ontologyFile = new File(ontologyFileName);
        ont = man.loadOntologyFromOntologyDocument(ontologyFile);
        LoggerManager.setFile(ontologyFile.getName());
        OWLDataFactory df = man.getOWLDataFactory();
        IRI iri = ont.getOntologyID().getOntologyIRI().get();
        OWLClass flag = df.getOWLClass(iri + "#assioma");
        boolean result = false;
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

    private boolean Hermit() {
        return oracle.isSatisfiable(expression.getNNF());
    }

    private boolean ALC_Reasoner(){
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

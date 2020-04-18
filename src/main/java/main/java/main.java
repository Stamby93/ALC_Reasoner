package main.java;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

import static org.semanticweb.owlapi.model.ClassExpressionType.OBJECT_INTERSECTION_OF;

public class main {

    public static void main(String[] args) throws Exception {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("AnotA.owl"));
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

        System.out.println("Concetto in input: " + expression.toString());

        /*

        if (expression.getClassExpressionType() == OBJECT_INTERSECTION_OF) {
            OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) expression;

            for (OWLClassExpression e : inter.operands().collect(Collectors.toSet())) {
                System.out.println("Operand " + e.toString());
            }
        }

        for (OWLClassExpression e : expression.nestedClassExpressions().collect(Collectors.toSet())) {
            ClassExpressionType type = e.getClassExpressionType();
            switch (type) {
                case OWL_CLASS:
                    System.out.println("Class: " + e.toString());
                    break;
                case OBJECT_SOME_VALUES_FROM:
                    System.out.println("Object Some Value: " + e.toString());
                    OWLObjectSomeValuesFrom casted = (OWLObjectSomeValuesFrom) e;
                    System.out.println("Object some Value Filler: " + casted.getFiller().toString());
                    break;
                case OBJECT_ALL_VALUES_FROM:
                    System.out.println("Object all Value: " + e.toString());
                    OWLObjectAllValuesFrom casted2 = (OWLObjectAllValuesFrom) e;
                    System.out.println("Object all Value Filler: " + casted2.getFiller().toString());
                    break;
                case OBJECT_HAS_VALUE:
                    System.out.println("Object Has Value: " + e.toString());
                    break;
                case OBJECT_HAS_SELF:
                    System.out.println("Object Has Self: " + e.toString());
                    break;
                case DATA_SOME_VALUES_FROM:
                    System.out.println("Data Some Value: " + e.toString());
                    break;
                case DATA_ALL_VALUES_FROM:
                    System.out.println("Data All Value: " + e.toString());
                    break;
                case DATA_HAS_VALUE:
                    System.out.println("Data Has Value: " + e.toString());
                    break;
                case OBJECT_INTERSECTION_OF:
                    System.out.println("Object Intersection: " + e.toString());
                    break;
                case OBJECT_UNION_OF:
                    System.out.println("Object Union: " + e.toString());
                    break;
                case OBJECT_COMPLEMENT_OF:
                    System.out.println("Object Complement of: " + e.toString());
                    break;
                case OBJECT_ONE_OF:
                    System.out.println("Object One of: " + e.toString());
                    break;
                default:
                    System.out.println("Unexpected Expression Type: " + e.toString());

            }
        }
    */

    OWLReasonerFactory ReasonerFactory = new ALCReasonerFactory();
    OWLReasoner ALCReasoner = ReasonerFactory.createReasoner(null);

            System.out.println("The concept is "+ALCReasoner.isSatisfiable(expression.getNNF()));


    }

}

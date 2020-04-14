import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;

public class main {

    public static void main(String[] args) throws Exception {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("prima.owl"));

        Set<OWLAxiom> expression = ont.axioms().collect(Collectors.toSet());
        System.out.println("NUMERO ESPRESSIONI: " + expression.size());
        int i = 0;
        for (OWLAxiom e : expression ) {
            i++;
                System.out.println("Numero: " + i + " = " + e.toString());
        }
        //OWLAxiom Concept[] = (OWLAxiom[]) ont.axioms().toArray();
        //int nConcept = Concept.length;

        /*OWLReasonerFactory ReasonerFactory = new ALCReasonerFactory();
        OWLReasoner ALCReasoner = ReasonerFactory.createReasoner(null);

        for(int i=0; i< nConcept; i++){
            OWLClassExpression Expression = new OWLObjectIntersectionOfImpl(Concept[i].nestedClassExpressions());
            System.out.println("The concept number" + (i + 1) + "is" + ALCReasoner.isSatisfiable(Expression));
        }
*/



    }

}

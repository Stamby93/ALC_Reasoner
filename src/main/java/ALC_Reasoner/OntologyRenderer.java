package ALC_Reasoner;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

/**
 * The Ontology renderer.
 */
public class OntologyRenderer {
    private static OWLObjectRenderer renderer;
    private static boolean initialized = false;

    private static void initRendered(){
        ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
        renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        renderer.setShortFormProvider(shortFormProvider);
        initialized = true;
    }

    /**
     * Render string.
     *
     * @param object OWLObject
     * @return String Rapresent the object
     */
    public static String render(OWLObject object){
        if(!initialized)
            initRendered();
        return renderer.render(object);
    }

}

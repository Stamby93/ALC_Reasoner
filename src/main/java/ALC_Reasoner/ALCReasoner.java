package ALC_Reasoner;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.util.Version;

import java.util.*;

/**
 * The ALC reasoner.
 */
public class ALCReasoner implements OWLReasoner {

    /**
     * The ALC_Reasoner.Tableau factory.
     */
    public TableauFactory TableauFactory;
    /**
     * The ALC_Reasoner.Tableau.
     */
    public Tableau Tableau = null;
    private String type = "Chronological";

    /**
     * Default constructor.
     */
    public ALCReasoner() {
        TableauFactory = new TableauFactory();
    }

    /**
     * Instantiates a new Alc reasoner.
     *
     * @param type String Represents the reasoning strategy to be used.
     */
    public ALCReasoner(String type){

        this.type = type;
        TableauFactory = new TableauFactory();
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public boolean isEntailed(OWLAxiom owlAxiom) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> set) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression owlClassExpression, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression owlClassExpression, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression owlClassExpression) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression owlClassExpression) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression owlObjectPropertyExpression, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression owlObjectPropertyExpression, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty owlDataProperty, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty owlDataProperty, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty owlDataProperty) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression owlDataPropertyExpression) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty owlDataProperty, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual owlNamedIndividual, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression owlClassExpression, boolean b) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual owlNamedIndividual, OWLObjectPropertyExpression owlObjectPropertyExpression) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual owlNamedIndividual, OWLDataProperty owlDataProperty) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual owlNamedIndividual) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual owlNamedIndividual) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public long getTimeOut() {
        return 0;
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public void dispose() {

    }

    @Override
    public String getReasonerName() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Version getReasonerVersion() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public BufferingMode getBufferingMode() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public OWLOntology getRootOntology() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public void interrupt() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public boolean isConsistent() {
        throw new UnsupportedOperationException("This is a simple ALC reasoner");
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression owlClassExpression) {

        Tableau = TableauFactory.getTableau(type, owlClassExpression.getNNF());
        return Tableau.SAT();
    }

    /**
     * Get model string.
     *
     * @return the string
     */
    public String getModel(){
        return Tableau.getModel();
    }

    /**
     * Get iteration integer.
     *
     * @return the integer
     */
    public Integer getIteration(){
        return Tableau.getIteration();
    }
}
package ALC_Reasoner;

/**
 * ALC_Reasoner.Tableau is the interface that provides the basic functionalities for the
 * development of reasoning techniques based precisely on the tableau,
 * for the ALC logic.
 */
public interface Tableau {

    /**
     * Sat boolean.
     *
     * @return The boolean value resulting from the reasoning task.
     */
    boolean SAT();

    /**
     * Gets model.
     *
     * @return When {@link #SAT()} returns true this method returns the model found during the reasoning, null otherwise.
     */
    String getModel();

    /**
     * Gets iteration.
     * The number of iterations needed for reasoning
     *
     * @return the iteration
     */
    Integer getIteration();

}
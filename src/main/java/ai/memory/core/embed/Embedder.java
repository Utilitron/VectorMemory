package ai.memory.core.embed;

/**
 * Converts text into a dense vector embedding for semantic search.
 */
public interface Embedder {

    /**
     * Generates an embedding vector for the given text.
     *
     * @param text the input text to embed
     * @return a float array representing the embedding (dimensions defined by {@link #dimensions()})
     */
    float[] embed(String text);

    /**
     * Returns the dimensionality of the embedding vectors produced by this embedder.
     *
     * @return the number of floating-point values in each embedding
     */
    int dimensions();
}
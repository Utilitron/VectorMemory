package ai.memory.core.index;

import java.util.List;

/**
 * A vector index for approximate nearest neighbour search.
 * <p>
 * Supports adding, removing, and searching vectors by ID.
 * Implementations should be thread‑safe for concurrent access.
 */
public interface VectorIndex {

    /**
     * Adds a vector to the index with the given ID.
     *
     * @param id     the identifier (e.g., memory trace ID)
     * @param vector the embedding vector (float array)
     */
    void add(String id, float[] vector);

    /**
     * Removes a vector from the index by its ID.
     * <p>
     * If the ID does not exist, the operation does nothing.
     *
     * @param id the identifier
     */
    void remove(String id);

    /**
     * Searches for the k nearest neighbours to the query vector.
     *
     * @param query the query vector
     * @param k     the maximum number of results to return
     * @return a list of IDs (strings) of the nearest neighbours,
     *         ordered by increasing distance (best first)
     */
    List<String> search(float[] query, int k);
}
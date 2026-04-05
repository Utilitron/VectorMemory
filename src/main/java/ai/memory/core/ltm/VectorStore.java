package ai.memory.core.ltm;

import ai.memory.core.model.SearchResult;

import java.util.List;
import java.util.Map;

/**
 * Vector database interface for storing embeddings and performing similarity search.
 */
public interface VectorStore {

    /**
     * Inserts or updates a vector with associated metadata.
     *
     * @param id       unique identifier for the vector entry
     * @param vector   the embedding array
     * @param metadata arbitrary key‑value metadata (e.g., docId, text, timestamp)
     */
    void upsert(String id, float[] vector, Map<String, Object> metadata);

    /**
     * Performs approximate nearest neighbour search for the given query vector.
     *
     * @param query the embedding vector to compare against stored vectors
     * @param k     the maximum number of results to return
     * @return a list of search results, each containing the ID, similarity score, and metadata
     */
    List<SearchResult> search(float[] query, int k);
}

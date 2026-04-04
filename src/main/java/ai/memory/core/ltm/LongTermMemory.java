package ai.memory.core.ltm;

import ai.memory.core.model.KnowledgeItem;

import java.util.List;
import java.util.Optional;

/**
 * Persistent, distilled memory that survives restarts.
 */
public interface LongTermMemory {

    /**
     * Stores a knowledge item (insert or update).
     *
     * @param item the item to store
     */
    void store(KnowledgeItem item);

    /**
     * Searches for knowledge items semantically similar to the query.
     *
     * @param query natural language query
     * @param k     maximum number of results
     * @return list of matching items, sorted by descending similarity
     */
    List<KnowledgeItem> search(String query, int k);

    /**
     * Retrieves an item by ID.
     *
     * @param id the item ID
     * @return the item, or empty if not found
     */
    Optional<KnowledgeItem> findById(String id);
}

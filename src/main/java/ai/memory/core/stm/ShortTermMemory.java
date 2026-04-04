package ai.memory.core.stm;

import ai.memory.core.model.MemoryTrace;

import java.util.List;

/**
 * Volatile, high‑speed working memory with vector similarity search.
 */
public interface ShortTermMemory {

    /**
     * Adds a memory trace to STM.
     *
     * @param trace the trace to add
     */
    void add(MemoryTrace trace);

    /**
     * Searches for traces semantically similar to the query text.
     *
     * @param query natural language query
     * @param k     maximum number of results
     * @return list of matching traces, sorted by descending similarity
     */
    List<MemoryTrace> search(String query, int k);

    /**
     * Returns the most recent traces (by timestamp), without vector search.
     *
     * @param limit maximum number of traces to return
     * @return recent traces, newest first
     */
    List<MemoryTrace> recent(int limit);

    /**
     * Removes a trace by ID (used during consolidation).
     *
     * @param id the trace ID
     */
    void remove(String id);
}

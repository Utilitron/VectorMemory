package ai.memory.core.store;

import ai.memory.core.model.MemoryTrace;

import java.util.Optional;

/**
 * A generic storage interface for memory traces.
 * <p>
 * Implementations may store traces in volatile memory (e.g., RAM for STM)
 * or persistently (e.g., disk or database for LTM).
 *
 * @param <T> the type of memory trace (must extend {@link MemoryTrace})
 */
public interface MemoryStore<T extends MemoryTrace> {

    /**
     * Stores a memory trace.
     * <p>
     * If a trace with the same ID already exists, it is overwritten.
     *
     * @param trace the trace to store
     */
    void remember(T trace);

    /**
     * Retrieves a memory trace by its unique identifier.
     *
     * @param id the trace ID
     * @return an {@code Optional} containing the trace if found, otherwise empty
     */
    Optional<T> findById(String id);
}
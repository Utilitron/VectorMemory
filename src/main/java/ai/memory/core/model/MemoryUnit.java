package ai.memory.core.model;

import java.util.Map;

/**
 * Base abstraction for all memory units in the VectorMemory system.
 * <p>
 * A memory unit can be either a volatile {@link MemoryTrace} (short‑term)
 * or a consolidated {@link KnowledgeItem} (long‑term).
 */
public sealed interface MemoryUnit permits MemoryTrace, KnowledgeItem {
    String id();
    String content();
    Map<String, Object> metadata();
}

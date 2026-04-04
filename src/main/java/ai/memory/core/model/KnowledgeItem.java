package ai.memory.core.model;

import java.util.Map;

/**
 * A consolidated, distilled knowledge item stored in long‑term memory.
 * <p>
 * Knowledge items are the result of the consolidation pipeline: they are
 * compressed, curated, and stable. They represent high‑density facts
 * extracted from many memory traces.
 *
 * @param id         unique identifier
 * @param content    distilled textual knowledge
 * @param metadata   metadata (source trace IDs, confidence, version, etc.)
 * @param version    version number (incremented on re‑consolidation)
 */
public record KnowledgeItem(
        String id,
        String content,
        Map<String, Object> metadata,
        int version
) implements MemoryUnit {}
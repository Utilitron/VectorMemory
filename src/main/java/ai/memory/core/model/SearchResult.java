package ai.memory.core.model;

import java.util.Map;

/**
 * Raw result from a vector store search before resolving the full text.
 *
 * @param id       identifier of the vector entry (typically a chunk ID)
 * @param score    similarity score from the vector search
 * @param metadata metadata stored with the vector (e.g., docId, text snippet)
 */
public record SearchResult(
        String id,
        float score,
        Map<String, Object> metadata
) {}
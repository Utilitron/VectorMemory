package ai.memory.core.model;

import java.util.Map;

/**
 * A raw, volatile memory trace stored in short‑term memory.
 * <p>
 * Traces are high‑volume, noisy, and short‑lived. They represent immediate
 * events: user messages, code edits, debug logs, etc.
 *
 * @param id        unique identifier of this trace
 * @param content   the textual content (event description)
 * @param timestamp creation time in milliseconds since epoch
 * @param metadata  mutable metadata (tags, importance, frequency, etc.)
 */
public record MemoryTrace(
        String id,
        String content,
        long timestamp,
        Map<String, Object> metadata
) implements MemoryUnit {}
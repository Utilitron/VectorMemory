package ai.memory.core.pipeline;

import ai.memory.core.model.KnowledgeItem;
import ai.memory.core.model.MemoryTrace;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Strategy for distilling a set of memory traces into a single knowledge item.
 */
@FunctionalInterface
public interface DistillationStrategy {

    /**
     * Distills a list of memory traces into a knowledge item.
     *
     * @param traces the traces to compress (low saliency, similar context)
     * @return a consolidated knowledge item
     */
    KnowledgeItem distill(List<MemoryTrace> traces);

    /**
     * A simple concatenation strategy (no LLM) – for testing.
     *
     * @return a strategy that joins trace contents with newlines
     */
    static DistillationStrategy concatenating() {
        return traces -> {
            String content = traces.stream()
                    .map(MemoryTrace::content)
                    .collect(Collectors.joining("\n---\n"));
            return new KnowledgeItem(
                    UUID.randomUUID().toString(),
                    content,
                    Map.of("distilled", true, "sourceCount", traces.size()),
                    1
            );
        };
    }
}
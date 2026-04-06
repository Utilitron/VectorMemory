package ai.memory.core;

import ai.memory.core.model.*;
import ai.memory.core.ltm.LongTermMemory;
import ai.memory.core.pipeline.Consolidator;
import ai.memory.core.stm.ShortTermMemory;
import ai.memory.core.strategy.SaliencyScorer;

import java.util.*;

/**
 * Main entry point for the VectorMemory system.
 */
public class MemoryService {

    private final ShortTermMemory stm;
    private final LongTermMemory ltm;
    private final SaliencyScorer scorer;

    /**
     * Creates a memory service.
     *
     * @param stm     short‑term memory
     * @param ltm     long‑term memory
     * @param scorer  saliency scorer (for tracking retrieval frequency)
     */
    public MemoryService(ShortTermMemory stm, LongTermMemory ltm, SaliencyScorer scorer) {
        this.stm = stm;
        this.ltm = ltm;
        this.scorer = scorer;
    }

    /**
     * Stores a new memory trace (raw event) into STM.
     *
     * @param content  the textual content
     * @param metadata metadata (tags, importance, etc.)
     */
    public void remember(String content, Map<String, Object> metadata) {
        MemoryTrace trace = new MemoryTrace(
                UUID.randomUUID().toString(),
                content,
                System.currentTimeMillis(),
                new HashMap<>(metadata)
        );
        stm.add(trace);
    }

    /**
     * Recalls memories relevant to the query, merging results from STM and LTM.
     *
     * @param query natural language query
     * @param k     maximum total results
     * @return list of memory units (both traces and knowledge items)
     */
    public List<MemoryUnit> recall(String query, int k) {
        int stmK = k / 2;
        int ltmK = k - stmK;

        List<MemoryTrace> stmResults = stm.search(query, stmK);
        for (MemoryTrace t : stmResults) {
            scorer.recordRetrieval(t.id());
        }

        List<KnowledgeItem> ltmResults = ltm.search(query, ltmK);

        List<MemoryUnit> merged = new ArrayList<>();
        merged.addAll(stmResults);
        merged.addAll(ltmResults);

        return merged;
    }

    /**
     * Triggers consolidation if system pressure is high.
     *
     * @param pressure current resource pressure (0.0–1.0)
     * @param consolidator the consolidator instance
     */
    public void tick(double pressure, Consolidator consolidator) {
        if (pressure > 0.85) {
            consolidator.consolidate();
        }
    }
}

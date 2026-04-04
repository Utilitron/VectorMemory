package ai.memory.core.strategy;

import ai.memory.core.model.MemoryTrace;
import ai.memory.core.util.Metadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Computes a saliency score for a memory trace using the RIF model:
 * Recency, Importance, Frequency.
 */
public class SaliencyScorer {

    private final Map<String, Integer> frequencyMap = new ConcurrentHashMap<>();
    private final long halfLifeMillis;

    /**
     * Creates a saliency scorer.
     *
     * @param halfLifeSeconds the half‑life for recency decay (in seconds)
     */
    public SaliencyScorer(int halfLifeSeconds) {
        this.halfLifeMillis = halfLifeSeconds * 1000L;
    }

    /**
     * Records a retrieval hit for a trace (increases its frequency).
     *
     * @param traceId the trace ID
     */
    public void recordRetrieval(String traceId) {
        frequencyMap.merge(traceId, 1, Integer::sum);
    }

    /**
     * Computes the saliency score for a trace.
     * <p>
     * Score = 0.4 * recency + 0.4 * importance + 0.2 * frequency
     *
     * @param trace the memory trace
     * @return a score in [0.0, 1.0], higher = more salient
     */
    public double score(MemoryTrace trace) {
        double recency = computeRecency(trace.timestamp());
        double importance = Metadata.getDouble(trace.metadata(), "importance", 0.5);
        double frequency = computeFrequency(trace.id());

        recency = Math.min(1.0, Math.max(0.0, recency));
        importance = Math.min(1.0, Math.max(0.0, importance));
        frequency = Math.min(1.0, frequency / 10.0);

        return 0.4 * recency + 0.4 * importance + 0.2 * frequency;
    }

    private double computeRecency(long timestampMillis) {
        long ageMillis = System.currentTimeMillis() - timestampMillis;
        if (ageMillis <= 0) return 1.0;
        return Math.pow(0.5, (double) ageMillis / halfLifeMillis);
    }

    private double computeFrequency(String traceId) {
        return frequencyMap.getOrDefault(traceId, 0);
    }
}
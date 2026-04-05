package ai.memory.core.stm;

import ai.memory.core.model.MemoryTrace;
import ai.memory.core.embed.Embedder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Deque;

/**
 * High‑performance STM using a pluggable VectorIndex (e.g., HnswIndex).
 */
public class VolatileSTM implements ShortTermMemory {

    private final Embedder embedder;
    private final VectorIndex vectorIndex;
    private final Map<String, MemoryTrace> traceMap = new ConcurrentHashMap<>();
    private final Deque<String> recentQueue = new ArrayDeque<>();
    private final int maxSize;

    /**
     * Creates a new JVector STM.
     *
     * @param embedder     converts text to embedding vectors
     * @param vectorIndex  the vector index implementation (e.g., HnswIndex)
     * @param maxSize      maximum number of traces before oldest are evicted
     */
    public VolatileSTM(Embedder embedder, VectorIndex vectorIndex, int maxSize) {
        this.embedder = embedder;
        this.vectorIndex = vectorIndex;
        this.maxSize = maxSize;
    }

    @Override
    public void add(MemoryTrace trace) {
        // Enforce size limit – simple FIFO eviction
        synchronized (recentQueue) {
            while (recentQueue.size() >= maxSize) {
                String oldestId = recentQueue.pollLast();
                if (oldestId != null) {
                    traceMap.remove(oldestId);
                    vectorIndex.remove(oldestId);
                }
            }
            recentQueue.addFirst(trace.id());
        }

        float[] vector = embedder.embed(trace.content());
        vectorIndex.add(trace.id(), vector);
        traceMap.put(trace.id(), trace);
    }

    @Override
    public List<MemoryTrace> search(String query, int k) {
        float[] queryVec = embedder.embed(query);
        List<String> ids = vectorIndex.search(queryVec, k);
        return ids.stream()
                .map(traceMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<MemoryTrace> recent(int limit) {
        synchronized (recentQueue) {
            return recentQueue.stream()
                    .limit(limit)
                    .map(traceMap::get)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    @Override
    public void remove(String id) {
        synchronized (recentQueue) {
            recentQueue.remove(id);
        }
        traceMap.remove(id);
        vectorIndex.remove(id);
    }
}
package ai.memory.core.index;

import io.github.jbellis.jvector.graph.GraphIndexBuilder;
import io.github.jbellis.jvector.graph.GraphSearcher;
import io.github.jbellis.jvector.graph.RandomAccessVectorValues;
import io.github.jbellis.jvector.graph.SearchResult;
import io.github.jbellis.jvector.graph.similarity.BuildScoreProvider;
import io.github.jbellis.jvector.graph.similarity.SearchScoreProvider;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * HNSW (Hierarchical Navigable Small World) index implementation using JVector.
 * <p>
 * This index stores vectors in memory and provides fast approximate nearest
 * neighbour search with cosine similarity. It supports incremental addition
 * and soft deletion of nodes.
 *
 * <p><b>Thread safety:</b> All public methods are synchronised to ensure
 * consistency of the underlying graph and internal maps.
 *
 * @see VectorIndex
 */
public class HnswIndex implements VectorIndex {

    private static final Logger log = LoggerFactory.getLogger(HnswIndex.class);

    /**
     * Adapts a {@link ConcurrentHashMap} of vectors to JVector's
     * {@link RandomAccessVectorValues} interface.
     *
     * @param map map from internal node ID to vector
     * @param dimension  vector dimensionality
     */
    private record InMemoryVectorValues(ConcurrentHashMap<Integer, float[]> map, int dimension)
            implements RandomAccessVectorValues { // No longer needs <float[]> generic

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public int dimension() {
            return dimension;
        }

        @Override
        public VectorFloat<?> getVector(int i) {
            float[] array = map.get(i);
            if (array == null) {
                throw new IllegalStateException("Missing vector for node " + i);
            }

            VectorTypeSupport typeSupport = VectorizationProvider.getInstance().getVectorTypeSupport();
            return typeSupport.createFloatVector(array);
        }

        @Override
        public boolean isValueShared() {
            return false;
        }

        @Override
        public RandomAccessVectorValues copy() {
            return this;
        }
    }

    private final GraphIndexBuilder builder;
    private final BuildScoreProvider scoreProvider;
    private final AtomicInteger nextId = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, String> idToEntryKey = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, float[]> idToVector = new ConcurrentHashMap<>();
    private final InMemoryVectorValues vectorValues;

    /**
     * Creates a new HNSW index with the given vector dimension.
     *
     * @param dimension the dimensionality of all vectors (must be consistent across adds)
     */
    public HnswIndex(int dimension) {
        this.vectorValues = new InMemoryVectorValues(idToVector, dimension);
        this.scoreProvider = BuildScoreProvider.randomAccessScoreProvider(vectorValues, VectorSimilarityFunction.COSINE);
        this.builder = new GraphIndexBuilder(
                scoreProvider,
                dimension,
                16,    // M
                100,   // beamWidth
                1.2f,  // neighborOverflow
                1.2f   // alpha
        );
    }

    /**
     * Adds a vector to the index.
     * <p>
     * The vector is assigned an internal node ID and inserted into the HNSW graph.
     *
     * @param id     the external identifier (e.g., memory trace ID)
     * @param vector the embedding vector
     */
    @Override
    public synchronized void add(String id, float[] vector) {
        if (vector.length != vectorValues.dimension()) {
            throw new IllegalArgumentException("dimension mismatch: expected " + vectorValues.dimension() + ", got " + vector.length);
        }

        int nodeId = nextId.getAndIncrement();

        idToEntryKey.put(nodeId, id);
        idToVector.put(nodeId, vector);

        var typeSupport = VectorizationProvider.getInstance().getVectorTypeSupport();
        var vectorFloat = typeSupport.createFloatVector(vector);

        builder.addGraphNode(nodeId, vectorFloat);
    }

    /**
     * Removes a vector from the index.
     * <p>
     * Removal is performed as a soft deletion – the node is marked as deleted
     * in the graph but remains in the internal maps to avoid index corruption.
     *
     * @param id the external identifier
     */
    @Override
    public synchronized void remove(String id) {
        Integer nodeId = idToEntryKey.entrySet().stream()
                .filter(entry -> entry.getValue().equals(id))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (nodeId != null) {
            builder.markNodeDeleted(nodeId);
            idToEntryKey.remove(nodeId);
            //idToVector.remove(nodeId);
            log.debug("Marked node {} for deletion in HNSW index", id);
        }
    }

    /**
     * Searches for the k nearest neighbours to the query vector.
     *
     * @param query the query vector
     * @param k     the number of results to return
     * @return a list of external IDs (strings) of the nearest neighbours,
     *         ordered by increasing distance (best first)
     */
    @Override
    public List<String> search(float[] query, int k) {
        if (query.length != vectorValues.dimension()) {
            throw new IllegalArgumentException("dimension mismatch");
        }

        if (idToVector.isEmpty()) {
            return List.of();
        }

        VectorTypeSupport typeSupport = VectorizationProvider.getInstance().getVectorTypeSupport();
        VectorFloat<?> queryVector = typeSupport.createFloatVector(query);
        SearchScoreProvider searchScoreProvider = scoreProvider.searchProviderFor(queryVector);
        var graph = builder.getGraph();

        try (GraphSearcher searcher = new GraphSearcher(graph)) {
            SearchResult results = searcher.search(
                    searchScoreProvider,
                    k,
                    Bits.ALL
            );

            List<String> keys = new ArrayList<>();
            for (var nodeScore : results.getNodes()) {
                String key = idToEntryKey.get(nodeScore.node);
                if (key != null) {
                    keys.add(key);
                }
            }
            return keys;
        } catch (Exception e) {
            log.error("HNSW Search failed", e);
            return List.of();
        }
    }
}
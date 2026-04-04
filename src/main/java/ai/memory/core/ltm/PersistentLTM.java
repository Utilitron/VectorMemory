package ai.memory.core.ltm;

import ai.memory.core.embed.Embedder;
import ai.memory.core.model.KnowledgeItem;
import ai.memory.core.model.SearchResult;
import ai.memory.core.store.VectorStore;
import ai.memory.core.util.Metadata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LTM backed by a vector store (e.g., H2 with vector extension, or external DB).
 */
public class PersistentLTM implements LongTermMemory {

    private final VectorStore vectorStore;
    private final Embedder embedder;
    private final Map<String, KnowledgeItem> cache = new ConcurrentHashMap<>();

    /**
     * Creates a persistent LTM.
     *
     * @param vectorStore the underlying vector store
     * @param embedder    embedder for search queries
     */
    public PersistentLTM(VectorStore vectorStore, Embedder embedder) {
        this.vectorStore = vectorStore;
        this.embedder = embedder;
    }

    @Override
    public void store(KnowledgeItem item) {
        float[] vector = embedder.embed(item.content());
        Map<String, Object> metadata = new HashMap<>(item.metadata());

        // Store content and version in metadata for persistence
        metadata.put("content", item.content());
        metadata.put("version", item.version());

        vectorStore.upsert(item.id(), vector, metadata);
        cache.put(item.id(), item);
    }

    @Override
    public List<KnowledgeItem> search(String query, int k) {
        float[] queryVec = embedder.embed(query);
        List<SearchResult> results = vectorStore.search(queryVec, k);

        return results.stream()
                .limit(k)
                .map(r -> cache.computeIfAbsent(r.id(), id -> {
                    String content = Metadata.getString(r.metadata(), "content", "");
                    int version = Metadata.getInt(r.metadata(), "version", 1);

                    // Clean internal fields from user-facing metadata
                    Map<String, Object> cleanMetadata = new HashMap<>(r.metadata());
                    cleanMetadata.remove("content");
                    cleanMetadata.remove("version");

                    return new KnowledgeItem(id, content, cleanMetadata, version);
                }))
                .toList();
    }

    @Override
    public Optional<KnowledgeItem> findById(String id) {
        return Optional.ofNullable(cache.get(id));
    }
}

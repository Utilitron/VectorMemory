package ai.memory.core.pipeline;

import ai.memory.core.model.KnowledgeItem;
import ai.memory.core.model.MemoryTrace;
import ai.memory.core.ltm.LongTermMemory;
import ai.memory.core.stm.ShortTermMemory;
import ai.memory.core.strategy.SaliencyScorer;

import java.util.Comparator;
import java.util.List;

/**
 * Runs the consolidation process: moves low‑saliency traces from STM to LTM
 * after distilling them into a knowledge item.
 */
public class Consolidator {

    private final ShortTermMemory stm;
    private final LongTermMemory ltm;
    private final SaliencyScorer scorer;
    private final DistillationStrategy distillationStrategy;
    private final int lowSaliencyCount;

    /**
     * Creates a consolidator.
     *
     * @param stm                  short‑term memory
     * @param ltm                  long‑term memory
     * @param scorer               RIF saliency scorer
     * @param distillationStrategy how to distill traces into a knowledge item
     * @param lowSaliencyCount     number of lowest‑saliency traces to consolidate per run
     */
    public Consolidator(ShortTermMemory stm,
                        LongTermMemory ltm,
                        SaliencyScorer scorer,
                        DistillationStrategy distillationStrategy,
                        int lowSaliencyCount) {
        this.stm = stm;
        this.ltm = ltm;
        this.scorer = scorer;
        this.distillationStrategy = distillationStrategy;
        this.lowSaliencyCount = lowSaliencyCount;
    }

    /**
     * Performs one consolidation cycle.
     */
    public void consolidate() {
        List<MemoryTrace> recent = stm.recent(1000);
        if (recent.size() < lowSaliencyCount) return;

        List<MemoryTrace> sorted = recent.stream()
                .sorted(Comparator.comparingDouble(scorer::score))
                .toList();

        List<MemoryTrace> toConsolidate = sorted.stream()
                .limit(lowSaliencyCount)
                .toList();

        if (toConsolidate.isEmpty()) return;

        KnowledgeItem distilled = distillationStrategy.distill(toConsolidate);
        ltm.store(distilled);

        for (MemoryTrace trace : toConsolidate) {
            stm.remove(trace.id());
        }
    }
}
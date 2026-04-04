package ai.memory.core.util;

import ai.memory.core.model.MemoryUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with tags stored in memory unit metadata.
 */
public final class MemoryTags {

    private MemoryTags() {}

    private static final String TAGS_KEY = "tags";

    /**
     * Checks whether a memory unit has a specific tag.
     *
     * @param unit the memory unit
     * @param tag  the tag to check
     * @return true if the unit's metadata contains the tag in the "tags" list
     */
    @SuppressWarnings("unchecked")
    public static boolean hasTag(MemoryUnit unit, String tag) {
        Object tagsObj = unit.metadata().get(TAGS_KEY);
        if (tagsObj instanceof List<?> list) {
            return list.contains(tag);
        }
        return false;
    }

    /**
     * Adds a tag to the memory unit's metadata (creates the list if absent).
     *
     * @param unit the memory unit
     * @param tag  the tag to add
     */
    @SuppressWarnings("unchecked")
    public static void addTag(MemoryUnit unit, String tag) {
        List<String> tags = (List<String>) unit.metadata()
                .computeIfAbsent(TAGS_KEY, k -> new ArrayList<String>());
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
}
package ai.memory.core.util;

import java.util.Map;

/**
 * Type‑safe accessors for common metadata keys.
 */
public final class Metadata {

    private Metadata() {}

    /**
     * Retrieves a double value from metadata, returning a default if missing or wrong type.
     *
     * @param metadata the metadata map
     * @param key      the key
     * @param def      default value
     * @return the double value or default
     */
    public static double getDouble(Map<String, Object> metadata, String key, double def) {
        Object v = metadata.get(key);
        return (v instanceof Number) ? ((Number) v).doubleValue() : def;
    }

    /**
     * Retrieves an integer value from metadata.
     *
     * @param metadata the metadata map
     * @param key      the key
     * @param def      default value
     * @return the integer or default
     */
    public static int getInt(Map<String, Object> metadata, String key, int def) {
        Object v = metadata.get(key);
        return (v instanceof Number) ? ((Number) v).intValue() : def;
    }

    /**
     * Retrieves a string from metadata.
     *
     * @param metadata the metadata map
     * @param key      the key
     * @param def      default value
     * @return the string or default
     */
    public static String getString(Map<String, Object> metadata, String key, String def) {
        Object v = metadata.get(key);
        return (v instanceof String) ? (String) v : def;
    }
}

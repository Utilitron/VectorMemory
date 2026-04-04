package ai.memory.core.pipeline;

/**
 * Measures system pressure that triggers memory consolidation.
 */
@FunctionalInterface
public interface ResourcePressure {
    /**
     * Returns the current pressure level.
     *
     * @return a value in [0.0, 1.0], where 1.0 means immediate consolidation required
     */
    double level();
}

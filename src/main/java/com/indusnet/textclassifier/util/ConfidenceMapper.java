package com.indusnet.textclassifier.util;

/**
 * Clamps an AI-reported confidence value to the [0.0, 1.0] range.
 *
 * <p>OpenAI's chat models do not natively output calibrated probabilities.
 * We ask the model to self-report a confidence value inside the structured
 * JSON prompt. This helper ensures that value is always within the expected
 * range regardless of what the model returns.
 *
 * <p><strong>Important:</strong> the resulting value reflects the model's
 * own assessment and is <em>not</em> a statistically calibrated probability.
 */
public class ConfidenceMapper {

    private ConfidenceMapper() {
        // Utility class – no instances needed
    }

    /**
     * Clamps {@code value} to the closed interval [0.0, 1.0] and rounds to
     * two decimal places for a clean API response.
     */
    public static double clamp(double value) {
        double clamped = Math.max(0.0, Math.min(1.0, value));
        return Math.round(clamped * 100.0) / 100.0;
    }

    /**
     * Returns {@code true} if the value is strictly within [0.0, 1.0].
     */
    public static boolean isValid(double value) {
        return value >= 0.0 && value <= 1.0;
    }
}

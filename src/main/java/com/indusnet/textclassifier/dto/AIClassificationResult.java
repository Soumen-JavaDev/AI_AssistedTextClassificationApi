package com.indusnet.textclassifier.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.indusnet.textclassifier.enums.ClassificationCategory;

/**
 * Holds the classification result parsed from the OpenAI response JSON.
 * Unknown JSON fields are ignored to be safe against unexpected model output.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIClassificationResult {

    private ClassificationCategory category;
    private double confidence;

    public AIClassificationResult() {}

    public AIClassificationResult(ClassificationCategory category, double confidence) {
        this.category = category;
        this.confidence = confidence;
    }

    public ClassificationCategory getCategory() {
        return category;
    }

    public void setCategory(ClassificationCategory category) {
        this.category = category;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}

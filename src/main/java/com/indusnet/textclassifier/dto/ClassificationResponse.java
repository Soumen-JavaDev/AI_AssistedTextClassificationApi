package com.indusnet.textclassifier.dto;

import com.indusnet.textclassifier.enums.ClassificationCategory;

/**
 * Successful response returned to the caller after classification.
 */
public class ClassificationResponse {

    private ClassificationCategory category;
    private double confidence;
    private String message;

    public ClassificationResponse() {}

    public ClassificationResponse(ClassificationCategory category, double confidence, String message) {
        this.category = category;
        this.confidence = confidence;
        this.message = message;
    }

    public ClassificationCategory getCategory() {
        return category;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getMessage() {
        return message;
    }
}

package com.indusnet.textclassifier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for the classification endpoint.
 * The 'text' field is validated before the service layer is reached.
 */
public class ClassificationRequest {

    @NotBlank(message = "Text must not be blank")
    @Size(max = 5000, message = "Text must not exceed 5000 characters")
    private String text;

    // Required by Jackson for deserialization
    public ClassificationRequest() {}

    public ClassificationRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

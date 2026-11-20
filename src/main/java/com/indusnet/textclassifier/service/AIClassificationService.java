package com.indusnet.textclassifier.service;

import com.indusnet.textclassifier.client.OpenAIClient;
import com.indusnet.textclassifier.dto.AIClassificationResult;
import com.indusnet.textclassifier.dto.ClassificationRequest;
import com.indusnet.textclassifier.dto.ClassificationResponse;
import com.indusnet.textclassifier.util.ConfidenceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the text classification workflow:
 * <ol>
 *   <li>Sends the validated text to the AI via {@link OpenAIClient}</li>
 *   <li>Clamps the confidence value to [0.0, 1.0]</li>
 *   <li>Builds and returns the response DTO</li>
 * </ol>
 *
 * Business logic lives here, not in the controller.
 */
@Service
public class AIClassificationService implements ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(AIClassificationService.class);

    private final OpenAIClient openAIClient;

    public AIClassificationService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @Override
    public ClassificationResponse classify(ClassificationRequest request) {
        log.info("Classifying text of length {}", request.getText().length());

        AIClassificationResult result = openAIClient.classify(request.getText());

        double confidence = ConfidenceMapper.clamp(result.getConfidence());

        log.info("Classification complete: category={}, confidence={}", result.getCategory(), confidence);

        return new ClassificationResponse(result.getCategory(), confidence, "Text classified successfully");
    }
}

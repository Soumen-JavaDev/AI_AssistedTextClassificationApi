package com.indusnet.textclassifier.service;

import com.indusnet.textclassifier.client.OpenAIClient;
import com.indusnet.textclassifier.dto.AIClassificationResult;
import com.indusnet.textclassifier.dto.ClassificationRequest;
import com.indusnet.textclassifier.dto.ClassificationResponse;
import com.indusnet.textclassifier.enums.ClassificationCategory;
import com.indusnet.textclassifier.exception.AIServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link AIClassificationService}.
 * No Spring context, no real HTTP calls.
 */
@ExtendWith(MockitoExtension.class)
class AIClassificationServiceTest {

    @Mock
    private OpenAIClient openAIClient;

    @InjectMocks
    private AIClassificationService classificationService;

    private ClassificationRequest request;

    @BeforeEach
    void setUp() {
        request = new ClassificationRequest("Sample input text");
    }

    // ─── Happy-path scenarios ────────────────────────────────────────────────

    @Test
    @DisplayName("Should return COMPLAINT when AI returns COMPLAINT")
    void shouldClassifyComplaint() {
        when(openAIClient.classify(anyString()))
                .thenReturn(new AIClassificationResult(ClassificationCategory.COMPLAINT, 0.92));

        ClassificationResponse response = classificationService.classify(request);

        assertThat(response.getCategory()).isEqualTo(ClassificationCategory.COMPLAINT);
        assertThat(response.getConfidence()).isEqualTo(0.92);
        assertThat(response.getMessage()).isEqualTo("Text classified successfully");
    }

    @Test
    @DisplayName("Should return QUERY when AI returns QUERY")
    void shouldClassifyQuery() {
        when(openAIClient.classify(anyString()))
                .thenReturn(new AIClassificationResult(ClassificationCategory.QUERY, 0.87));

        ClassificationResponse response = classificationService.classify(request);

        assertThat(response.getCategory()).isEqualTo(ClassificationCategory.QUERY);
        assertThat(response.getConfidence()).isEqualTo(0.87);
    }

    @Test
    @DisplayName("Should return FEEDBACK when AI returns FEEDBACK")
    void shouldClassifyFeedback() {
        when(openAIClient.classify(anyString()))
                .thenReturn(new AIClassificationResult(ClassificationCategory.FEEDBACK, 0.78));

        ClassificationResponse response = classificationService.classify(request);

        assertThat(response.getCategory()).isEqualTo(ClassificationCategory.FEEDBACK);
        assertThat(response.getConfidence()).isEqualTo(0.78);
    }

    @Test
    @DisplayName("Should return OTHER when AI returns OTHER")
    void shouldClassifyOther() {
        when(openAIClient.classify(anyString()))
                .thenReturn(new AIClassificationResult(ClassificationCategory.OTHER, 0.60));

        ClassificationResponse response = classificationService.classify(request);

        assertThat(response.getCategory()).isEqualTo(ClassificationCategory.OTHER);
    }

    // ─── Confidence boundary tests ───────────────────────────────────────────

    @Test
    @DisplayName("Should clamp confidence above 1.0 to 1.0")
    void shouldClampConfidenceAboveOne() {
        when(openAIClient.classify(anyString()))
                .thenReturn(new AIClassificationResult(ClassificationCategory.COMPLAINT, 1.5));

        ClassificationResponse response = classificationService.classify(request);

        assertThat(response.getConfidence()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should clamp confidence below 0.0 to 0.0")
    void shouldClampConfidenceBelowZero() {
        when(openAIClient.classify(anyString()))
                .thenReturn(new AIClassificationResult(ClassificationCategory.COMPLAINT, -0.3));

        ClassificationResponse response = classificationService.classify(request);

        assertThat(response.getConfidence()).isEqualTo(0.0);
    }

    // ─── Error propagation tests ─────────────────────────────────────────────

    @Test
    @DisplayName("Should propagate AIServiceException when AI client fails")
    void shouldPropagateAIServiceException() {
        when(openAIClient.classify(anyString()))
                .thenThrow(new AIServiceException("Could not parse the AI response"));

        assertThatThrownBy(() -> classificationService.classify(request))
                .isInstanceOf(AIServiceException.class)
                .hasMessageContaining("Could not parse the AI response");
    }

    @Test
    @DisplayName("Should propagate AIServiceException on AI service failure")
    void shouldPropagateOnAIServiceFailure() {
        when(openAIClient.classify(anyString()))
                .thenThrow(new AIServiceException("AI API server error", 503));

        assertThatThrownBy(() -> classificationService.classify(request))
                .isInstanceOf(AIServiceException.class);
    }
}

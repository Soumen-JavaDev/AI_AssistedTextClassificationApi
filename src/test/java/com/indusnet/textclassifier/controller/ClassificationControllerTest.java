package com.indusnet.textclassifier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indusnet.textclassifier.dto.ClassificationRequest;
import com.indusnet.textclassifier.dto.ClassificationResponse;
import com.indusnet.textclassifier.enums.ClassificationCategory;
import com.indusnet.textclassifier.exception.AIServiceException;
import com.indusnet.textclassifier.service.ClassificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for the controller layer.
 * The service is mocked – no Spring context, no real AI calls.
 */
@WebMvcTest(ClassificationController.class)
class ClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClassificationService classificationService;

    private static final String URL = "/api/v1/classify";

    // ─── Happy-path tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return COMPLAINT classification")
    void shouldReturnComplaintClassification() throws Exception {
        ClassificationResponse mockResponse = new ClassificationResponse(
                ClassificationCategory.COMPLAINT, 0.94, "Text classified successfully");

        when(classificationService.classify(any())).thenReturn(mockResponse);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest(
                                "I am unable to access my account even after resetting my password."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("COMPLAINT"))
                .andExpect(jsonPath("$.confidence").value(0.94))
                .andExpect(jsonPath("$.message").value("Text classified successfully"));
    }

    @Test
    @DisplayName("Should return QUERY classification")
    void shouldReturnQueryClassification() throws Exception {
        ClassificationResponse mockResponse = new ClassificationResponse(
                ClassificationCategory.QUERY, 0.88, "Text classified successfully");

        when(classificationService.classify(any())).thenReturn(mockResponse);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest("What are your business hours?"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("QUERY"));
    }

    @Test
    @DisplayName("Should return FEEDBACK classification")
    void shouldReturnFeedbackClassification() throws Exception {
        ClassificationResponse mockResponse = new ClassificationResponse(
                ClassificationCategory.FEEDBACK, 0.91, "Text classified successfully");

        when(classificationService.classify(any())).thenReturn(mockResponse);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest("Your app is great but the UI could be more intuitive."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("FEEDBACK"));
    }

    @Test
    @DisplayName("Should return OTHER classification")
    void shouldReturnOtherClassification() throws Exception {
        ClassificationResponse mockResponse = new ClassificationResponse(
                ClassificationCategory.OTHER, 0.75, "Text classified successfully");

        when(classificationService.classify(any())).thenReturn(mockResponse);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest("The quick brown fox jumps over the lazy dog."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("OTHER"));
    }

    // ─── Validation failure tests ────────────────────────────────────────────

    @Test
    @DisplayName("Should return 400 for empty text")
    void shouldReturn400ForEmptyText() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("Should return 400 for null text")
    void shouldReturn400ForNullText() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("Should return 400 for missing request body")
    void shouldReturn400ForMissingBody() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("Should return 400 for text exceeding 5000 characters")
    void shouldReturn400ForTextTooLong() throws Exception {
        String longText = "a".repeat(5001);
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest(longText))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    // ─── Error propagation tests ─────────────────────────────────────────────

    @Test
    @DisplayName("Should return 401 when AI reports authentication error")
    void shouldReturn401OnAuthError() throws Exception {
        when(classificationService.classify(any()))
                .thenThrow(new AIServiceException("Unauthorized", 401));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest("Some text"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AI_AUTH_ERROR"));
    }

    @Test
    @DisplayName("Should return 503 when AI service rate limited")
    void shouldReturn503OnRateLimit() throws Exception {
        when(classificationService.classify(any()))
                .thenThrow(new AIServiceException("Rate limit exceeded", 429));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest("Some text"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("AI_RATE_LIMIT"));
    }

    @Test
    @DisplayName("Should return 502 for invalid AI response")
    void shouldReturn502ForInvalidAIResponse() throws Exception {
        when(classificationService.classify(any()))
                .thenThrow(new AIServiceException("Could not parse the AI response"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ClassificationRequest("Some text"))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("AI_SERVICE_ERROR"));
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}

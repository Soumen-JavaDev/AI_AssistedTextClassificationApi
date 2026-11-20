package com.indusnet.textclassifier.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised error handling. Every exception that escapes the service layer ends
 * up here. Full detail is logged internally; only a safe, structured JSON body is
 * returned to the caller.
 *
 * <p>AI error codes returned to clients:
 * <ul>
 *   <li>{@code AI_AUTH_ERROR}      – API key invalid (OpenAI 401)</li>
 *   <li>{@code AI_QUOTA_EXCEEDED}  – billing quota exhausted (OpenAI insufficient_quota)</li>
 *   <li>{@code AI_RATE_LIMIT}      – too many requests (OpenAI 429)</li>
 *   <li>{@code AI_MODEL_ERROR}     – model name wrong or not on current plan</li>
 *   <li>{@code AI_TIMEOUT}         – upstream did not respond in time</li>
 *   <li>{@code AI_UNAVAILABLE}     – could not reach the AI service</li>
 *   <li>{@code AI_SERVICE_ERROR}   – any other upstream failure</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Validation failures (@NotBlank, @Size, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return errorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", detail);
    }

    // Malformed or missing JSON body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMalformedJson(HttpMessageNotReadableException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request body is missing or malformed");
    }

    // AI-layer problems – each upstream status maps to a distinct client-facing error code
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<Map<String, String>> handleAIServiceException(AIServiceException ex) {
        log.error("AI service error (upstream status={}): {}", ex.getStatusCode(), ex.getMessage());

        int upstream = ex.getStatusCode();

        // 401 – API key is invalid or missing
        if (upstream == 401) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "AI_AUTH_ERROR",
                    "Authentication with the AI service failed. Verify your OPENAI_API_KEY.");
        }
        // 402 – billing quota exhausted (OpenAI sends this as insufficient_quota inside a 429,
        //         but we normalise it to 402 in OpenAIClient for clarity)
        if (upstream == 402) {
            return errorResponse(HttpStatus.PAYMENT_REQUIRED, "AI_QUOTA_EXCEEDED",
                    "Your OpenAI account has run out of quota. Add credits at platform.openai.com.");
        }
        // 404 – model name is wrong or not available on the current plan
        if (upstream == 404) {
            return errorResponse(HttpStatus.BAD_GATEWAY, "AI_MODEL_ERROR",
                    "The configured AI model was not found. Check OPENAI_MODEL in your environment.");
        }
        // 408 – timeout set by our client
        if (upstream == 408) {
            return errorResponse(HttpStatus.GATEWAY_TIMEOUT, "AI_TIMEOUT",
                    "The AI service did not respond in time. Please try again.");
        }
        // 429 – genuine rate-limit (too many requests per minute)
        if (upstream == 429) {
            return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "AI_RATE_LIMIT",
                    "AI service rate limit reached. Please try again later.");
        }
        // 5xx – upstream server error
        if (upstream >= 500) {
            return errorResponse(HttpStatus.BAD_GATEWAY, "AI_SERVICE_ERROR",
                    "The AI service returned a server error. Please try again later.");
        }
        // statusCode == 0 means a local connectivity or parse failure
        if (upstream == 0 && ex.getCause() != null) {
            String causeType = ex.getCause().getClass().getSimpleName();
            if (causeType.contains("Timeout")) {
                return errorResponse(HttpStatus.GATEWAY_TIMEOUT, "AI_TIMEOUT",
                        "Connection to the AI service timed out.");
            }
            return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE",
                    "Could not connect to the AI service. Please try again later.");
        }

        // Fallback for any other upstream code (e.g. unknown 4xx)
        return errorResponse(HttpStatus.BAD_GATEWAY, "AI_SERVICE_ERROR", ex.getMessage());
    }

    // Catch-all for anything unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again.");
    }

    private ResponseEntity<Map<String, String>> errorResponse(HttpStatus status, String error, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}

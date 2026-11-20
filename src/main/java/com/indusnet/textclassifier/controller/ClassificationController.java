package com.indusnet.textclassifier.controller;

import com.indusnet.textclassifier.dto.ClassificationRequest;
import com.indusnet.textclassifier.dto.ClassificationResponse;
import com.indusnet.textclassifier.service.ClassificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the single business endpoint of this API.
 *
 * The controller does nothing except validate the incoming request and
 * delegate to the service layer. All business logic lives in
 * {@link com.indusnet.textclassifier.service.AIClassificationService}.
 */
@RestController
@RequestMapping("/api/v1")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    /**
     * POST /api/v1/classify
     *
     * <p>Accepts a JSON body with a {@code text} field, sends it to the AI
     * classification service, and returns the predicted category with a
     * confidence score.
     */
    @PostMapping("/classify")
    public ResponseEntity<ClassificationResponse> classify(@Valid @RequestBody ClassificationRequest request) {
        ClassificationResponse response = classificationService.classify(request);
        return ResponseEntity.ok(response);
    }
}

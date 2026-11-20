package com.indusnet.textclassifier.service;

import com.indusnet.textclassifier.dto.ClassificationRequest;
import com.indusnet.textclassifier.dto.ClassificationResponse;

public interface ClassificationService {

    ClassificationResponse classify(ClassificationRequest request);
}

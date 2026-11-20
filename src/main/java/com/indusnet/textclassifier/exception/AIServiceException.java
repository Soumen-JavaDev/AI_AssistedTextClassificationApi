package com.indusnet.textclassifier.exception;

/**
 * Thrown when the OpenAI API returns an unexpected response,
 * or when connectivity / authentication problems occur.
 */
public class AIServiceException extends RuntimeException {

    private final int statusCode;

    public AIServiceException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public AIServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

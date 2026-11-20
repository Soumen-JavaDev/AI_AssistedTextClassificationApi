package com.indusnet.textclassifier.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Creates a pre-configured {@link RestClient} bean for all OpenAI API calls.
 *
 * <p>The API key is read from the {@code OPENAI_API_KEY} environment variable
 * (or from a {@code .env} file in the project root). It is never hardcoded here
 * or logged anywhere in the application.
 *
 * <p>If the key is absent or blank the application will refuse to start, so the
 * problem is caught immediately rather than producing a confusing 401 at runtime.
 */
@Configuration
public class OpenAIConfig {

    @Value("${openai.base-url}")
    private String baseUrl;

    @Value("${openai.api-key}")
    private String apiKey;

    @Bean
    public RestClient openAIRestClient() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is not set. " +
                    "Set it as an environment variable or add it to the .env file in the project root.");
        }

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

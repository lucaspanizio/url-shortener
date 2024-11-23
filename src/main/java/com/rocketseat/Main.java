package com.rocketseat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@SuppressWarnings("unused")
public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {
    private static final int SHORT_URL_LENGTH = 8;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        String body = input.get("body").toString();
        if (body == null || body.isBlank()) {
            return createResponse("error", "Request body cannot be empty.");
        }

        Map<String, String> bodyMap = parseJsonBody(body);

        ObjectNode urlData = createUrlData(bodyMap);

        String shortUrlCode = generateShortUrlCode();

        saveUrlDataToS3(urlData, shortUrlCode, context);

        return createResponse("code", shortUrlCode);
    }

    private Map<String, String> parseJsonBody(String body) {
        try {
            return objectMapper.readValue(body, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return createResponse("error", "Error parsing JSON body: " + e.getMessage());
        }
    }

    private boolean validateRequiredFields(String originalUrl, String expirationTime) {
        return originalUrl != null && !originalUrl.isBlank() && expirationTime != null && !expirationTime.isBlank();
    }

    private String generateShortUrlCode() {
        return UUID.randomUUID().toString().substring(0, SHORT_URL_LENGTH);
    }

    private ObjectNode createUrlData(Map<String, String> bodyMap) {
        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");

        if (!validateRequiredFields(originalUrl, expirationTime)) {
            throw new IllegalArgumentException("Missing required fields.");
        }

        ObjectNode urlData = objectMapper.createObjectNode();
        urlData.put("originalUrl", originalUrl);
        urlData.put("expirationTime", Long.parseLong(expirationTime));
        return urlData;
    }

    private void saveUrlDataToS3(ObjectNode urlData, String key, Context context) {
        try {
            String urlDataJson = objectMapper.writeValueAsString(urlData);
            String bucketName = System.getenv("S3_BUCKET_NAME");

            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key + ".json")
                .build();

            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
            context.getLogger().log("Successfully stored URL data with code: " + key);
        } catch (Exception exception) {
            String error = "Error saving data to S3: " + exception.getMessage();
            context.getLogger().log(error);
            throw new RuntimeException(error, exception);
        }
    }

    private Map<String, String> createResponse(String key, String value) {
        Map<String, String> response = new HashMap<>();
        response.put(key, value);
        return response;
    }
}
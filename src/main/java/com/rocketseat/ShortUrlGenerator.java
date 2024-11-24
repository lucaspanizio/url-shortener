package com.rocketseat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@SuppressWarnings("unused")
public class ShortUrlGenerator implements RequestHandler<Map<String, Object>, Map<String, String>> {
    private static final int SHORT_URL_LENGTH = 8;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        String body = input.get("body").toString();
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body cannot be empty.");
        }

        Map<String, String> bodyMap;
        try {
            bodyMap = objectMapper.readValue(body, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON body: " + e.getMessage(), e);
        }

        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");
        long expirationTimeInSeconds = Long.parseLong(expirationTime);

        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("Missing required field: originalUrl.");
        }

        if (expirationTimeInSeconds <= 0) {
            throw new IllegalArgumentException("Invalid value for expirationTime.");
        }

        String shortUrlCode = UUID.randomUUID().toString().substring(0, SHORT_URL_LENGTH);

        try {
            UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);
            String urlDataJson = objectMapper.writeValueAsString(urlData);
            String bucketName = System.getenv("S3_BUCKET_NAME");

            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(shortUrlCode + ".json")
                .build();

            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
            context.getLogger().log("Successfully stored URL data with code: " + shortUrlCode);
        } catch (Exception exception) {
            String error = "Error saving data to S3: " + exception.getMessage();
            context.getLogger().log(error);
            throw new RuntimeException(error, exception);
        }

        Map<String, String> response = new HashMap<>();
        response.put("code", shortUrlCode);

        return response;
    }
}
package com.rocketseat;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ShortUrlResolver implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client s3Client = S3Client.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String pathParameters = input.get("rawPath").toString();
        String shortUrlCode = pathParameters.replace("/", "");

        if (shortUrlCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid short URL code");
        }

        String bucketName = System.getenv("S3_BUCKET_NAME");
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(shortUrlCode + ".json")
            .build();

        InputStream object;
        try {
            object = s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error getting data from S3: " + e.getMessage(), e);
        }

        Map<String, Object> response = new HashMap<>();

        if (object == null) {
            response.put("statusCode", 404);
            response.put("body", "Short URL code not found");
            return response;
        }

        UrlData urlData;
        try {
            urlData = objectMapper.readValue(object, UrlData.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing data from S3: " + e.getMessage(), e);
        }

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        if (currentTimeInSeconds > urlData.getExpirationTime()) {
            response.put("statusCode", 410);
            response.put("body", "This URL has expired");
            return response;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> queryStringParameters = (Map<String, String>) input.get("queryStringParameters");

        String redirectParam = (queryStringParameters != null && queryStringParameters.containsKey("redirect"))
                ? queryStringParameters.get("redirect")
                : "F";

        if (redirectParam.equalsIgnoreCase("T")) {
            response.put("statusCode", 302);
            response.put("headers", Map.of("Location", urlData.getOriginalUrl()));
        } else {
            response.put("statusCode", 200);
            response.put("body", urlData.getOriginalUrl());
        }

        return response;
    }
}
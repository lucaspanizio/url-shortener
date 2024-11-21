package com.rocketseat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {
    private static final int SHORT_URL_LENGTH = 8;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        String body = input.get("body").toString();
        if (body == null || body.isBlank()) {
            return createErrorResponse("Request body is empty or null");
        }

        Map<String, String> bodyMap;
        try {
            bodyMap = objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            return createErrorResponse("Error parsing JSON body: " + e.getMessage());
        }

        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");

        if (originalUrl == null || originalUrl.isBlank() || expirationTime == null || expirationTime.isBlank()) {
            return createErrorResponse("Missing required fields: originalUrl or expirationTime");
        }

        String shortUrlCode = UUID.randomUUID().toString().substring(0, SHORT_URL_LENGTH);

        Map<String, String> response = new HashMap<>();
        response.put("code", shortUrlCode);

        return response;
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }
}
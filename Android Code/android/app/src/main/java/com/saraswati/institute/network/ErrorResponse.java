package com.saraswati.institute.network;

import java.util.Collections;
import java.util.Map;

/**
 * Error body produced by {@code GlobalExceptionHandler} for every failed call:
 * <pre>{"timestamp":"…","status":400,"error":"Bad Request","message":"…","path":"…","fieldErrors":{…}}</pre>
 *
 * <p>{@code fieldErrors} is only present for validation failures (HTTP 400) and maps a
 * request field name to its message.
 */
public class ErrorResponse {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors != null ? fieldErrors : Collections.<String, String>emptyMap();
    }
}

package com.saraswati.institute.network;

/**
 * Envelope returned by every saraswati-auth endpoint:
 * <pre>{ "success": true, "message": "…", "data": { … } }</pre>
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public boolean isSuccess()  { return success; }
    public String  getMessage() { return message; }
    public T       getData()    { return data; }
}

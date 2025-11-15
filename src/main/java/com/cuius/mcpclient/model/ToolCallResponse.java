package com.cuius.mcpclient.model;

public record ToolCallResponse(
        boolean success,
        Object result,
        String error
) {
    public static ToolCallResponse success(Object result) {
        return new ToolCallResponse(true, result, null);
    }

    public static ToolCallResponse error(String error) {
        return new ToolCallResponse(false, null, error);
    }
}

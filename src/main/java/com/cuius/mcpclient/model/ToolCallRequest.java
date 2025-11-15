package com.cuius.mcpclient.model;

import java.util.Map;

public record ToolCallRequest(
        String toolName,
        Map<String, Object> arguments
) {
}

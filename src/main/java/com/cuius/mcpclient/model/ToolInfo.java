package com.cuius.mcpclient.model;

import java.util.Map;

public record ToolInfo(
        String name,
        String description,
        Map<String, Object> inputSchema
) {
}

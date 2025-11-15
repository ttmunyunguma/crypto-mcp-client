package com.cuius.mcpclient.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for model record classes.
 * Tests JSON serialization/deserialization and record behavior.
 */
class ModelClassesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ToolInfo Tests
    @Test
    void toolInfo_ShouldCreateInstanceWithAllFields() {
        // Arrange
        String name = "get_crypto_price";
        String description = "Get cryptocurrency price";
        Map<String, Object> inputSchema = Map.of("symbol", Map.of("type", "string"));

        // Act
        ToolInfo toolInfo = new ToolInfo(name, description, inputSchema);

        // Assert
        assertThat(toolInfo.name()).isEqualTo(name);
        assertThat(toolInfo.description()).isEqualTo(description);
        assertThat(toolInfo.inputSchema()).isEqualTo(inputSchema);
    }

    @Test
    void toolInfo_ShouldHandleNullDescription() {
        // Act
        ToolInfo toolInfo = new ToolInfo("tool_name", null, Map.of());

        // Assert
        assertThat(toolInfo.description()).isNull();
    }

    @Test
    void toolInfo_ShouldHandleNullInputSchema() {
        // Act
        ToolInfo toolInfo = new ToolInfo("tool_name", "description", null);

        // Assert
        assertThat(toolInfo.inputSchema()).isNull();
    }

    @Test
    void toolInfo_ShouldHandleEmptyInputSchema() {
        // Act
        ToolInfo toolInfo = new ToolInfo("tool_name", "description", Map.of());

        // Assert
        assertThat(toolInfo.inputSchema()).isEmpty();
    }

    @Test
    void toolInfo_ShouldHandleComplexInputSchema() {
        // Arrange
        Map<String, Object> complexSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "symbol", Map.of("type", "string", "required", true),
                        "currency", Map.of("type", "string", "default", "USD"),
                        "options", Map.of("type", "object")
                ),
                "required", java.util.List.of("symbol")
        );

        // Act
        ToolInfo toolInfo = new ToolInfo("complex_tool", "Complex tool", complexSchema);

        // Assert
        assertThat(toolInfo.inputSchema()).containsKeys("type", "properties", "required");
    }

    @Test
    void toolInfo_ShouldSerializeToJson() throws Exception {
        // Arrange
        ToolInfo toolInfo = new ToolInfo("test_tool", "Test description",
                Map.of("param", Map.of("type", "string")));

        // Act
        String json = objectMapper.writeValueAsString(toolInfo);

        // Assert
        assertThat(json).contains("test_tool");
        assertThat(json).contains("Test description");
        assertThat(json).contains("param");
    }

    @Test
    void toolInfo_ShouldDeserializeFromJson() throws Exception {
        // Arrange
        String json = "{\"name\":\"test_tool\",\"description\":\"Test description\"," +
                "\"inputSchema\":{\"param\":{\"type\":\"string\"}}}";

        // Act
        ToolInfo toolInfo = objectMapper.readValue(json, ToolInfo.class);

        // Assert
        assertThat(toolInfo.name()).isEqualTo("test_tool");
        assertThat(toolInfo.description()).isEqualTo("Test description");
        assertThat(toolInfo.inputSchema()).containsKey("param");
    }

    @Test
    void toolInfo_ShouldSupportEquality() {
        // Arrange
        Map<String, Object> schema = Map.of("param", "value");
        ToolInfo tool1 = new ToolInfo("name", "desc", schema);
        ToolInfo tool2 = new ToolInfo("name", "desc", schema);

        // Assert
        assertThat(tool1).isEqualTo(tool2);
        assertThat(tool1.hashCode()).isEqualTo(tool2.hashCode());
    }

    // ToolCallRequest Tests
    @Test
    void toolCallRequest_ShouldCreateInstanceWithAllFields() {
        // Arrange
        String toolName = "get_crypto_price";
        Map<String, Object> arguments = Map.of("symbol", "BTC", "currency", "USD");

        // Act
        ToolCallRequest request = new ToolCallRequest(toolName, arguments);

        // Assert
        assertThat(request.toolName()).isEqualTo(toolName);
        assertThat(request.arguments()).isEqualTo(arguments);
    }

    @Test
    void toolCallRequest_ShouldHandleNullArguments() {
        // Act
        ToolCallRequest request = new ToolCallRequest("tool_name", null);

        // Assert
        assertThat(request.arguments()).isNull();
    }

    @Test
    void toolCallRequest_ShouldHandleEmptyArguments() {
        // Act
        ToolCallRequest request = new ToolCallRequest("tool_name", Map.of());

        // Assert
        assertThat(request.arguments()).isEmpty();
    }

    @Test
    void toolCallRequest_ShouldHandleComplexArguments() {
        // Arrange
        Map<String, Object> complexArgs = Map.of(
                "stringParam", "value",
                "numberParam", 42,
                "booleanParam", true,
                "arrayParam", java.util.List.of(1, 2, 3),
                "objectParam", Map.of("nested", "data")
        );

        // Act
        ToolCallRequest request = new ToolCallRequest("complex_tool", complexArgs);

        // Assert
        assertThat(request.arguments()).hasSize(5);
        assertThat(request.arguments()).containsKeys("stringParam", "numberParam", "booleanParam", "arrayParam", "objectParam");
    }

    @Test
    void toolCallRequest_ShouldSerializeToJson() throws Exception {
        // Arrange
        ToolCallRequest request = new ToolCallRequest("test_tool",
                Map.of("param1", "value1", "param2", 123));

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertThat(json).contains("test_tool");
        assertThat(json).contains("param1");
        assertThat(json).contains("value1");
    }

    @Test
    void toolCallRequest_ShouldDeserializeFromJson() throws Exception {
        // Arrange
        String json = "{\"toolName\":\"test_tool\",\"arguments\":{\"param\":\"value\"}}";

        // Act
        ToolCallRequest request = objectMapper.readValue(json, ToolCallRequest.class);

        // Assert
        assertThat(request.toolName()).isEqualTo("test_tool");
        assertThat(request.arguments()).containsKey("param");
    }

    @Test
    void toolCallRequest_ShouldSupportEquality() {
        // Arrange
        Map<String, Object> args = Map.of("param", "value");
        ToolCallRequest request1 = new ToolCallRequest("tool", args);
        ToolCallRequest request2 = new ToolCallRequest("tool", args);

        // Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    // ToolCallResponse Tests
    @Test
    void toolCallResponse_ShouldCreateSuccessResponse() {
        // Arrange
        Object result = Map.of("price", 50000.00, "timestamp", "2024-01-01T00:00:00Z");

        // Act
        ToolCallResponse response = ToolCallResponse.success(result);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isEqualTo(result);
        assertThat(response.error()).isNull();
    }

    @Test
    void toolCallResponse_ShouldCreateErrorResponse() {
        // Arrange
        String error = "Tool not found";

        // Act
        ToolCallResponse response = ToolCallResponse.error(error);

        // Assert
        assertThat(response.success()).isFalse();
        assertThat(response.result()).isNull();
        assertThat(response.error()).isEqualTo(error);
    }

    @Test
    void toolCallResponse_ShouldHandleNullResult() {
        // Act
        ToolCallResponse response = ToolCallResponse.success(null);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isNull();
        assertThat(response.error()).isNull();
    }

    @Test
    void toolCallResponse_ShouldHandleNullError() {
        // Act
        ToolCallResponse response = ToolCallResponse.error(null);

        // Assert
        assertThat(response.success()).isFalse();
        assertThat(response.result()).isNull();
        assertThat(response.error()).isNull();
    }

    @Test
    void toolCallResponse_ShouldHandleComplexResult() {
        // Arrange
        Map<String, Object> complexResult = Map.of(
                "data", Map.of(
                        "price", 50000.00,
                        "volume", 1234567890,
                        "marketCap", 987654321098L
                ),
                "metadata", Map.of(
                        "timestamp", "2024-01-01T00:00:00Z",
                        "source", "exchange"
                )
        );

        // Act
        ToolCallResponse response = ToolCallResponse.success(complexResult);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) response.result();
        assertThat(resultMap).containsKeys("data", "metadata");
    }

    @Test
    void toolCallResponse_ShouldSerializeToJson() throws Exception {
        // Arrange
        ToolCallResponse response = ToolCallResponse.success(Map.of("result", "data"));

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("result");
        assertThat(json).contains("data");
    }

    @Test
    void toolCallResponse_ShouldDeserializeFromJson() throws Exception {
        // Arrange
        String json = "{\"success\":true,\"result\":{\"data\":\"value\"},\"error\":null}";

        // Act
        ToolCallResponse response = objectMapper.readValue(json, ToolCallResponse.class);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isNotNull();
        assertThat(response.error()).isNull();
    }

    @Test
    void toolCallResponse_ShouldSupportEquality() {
        // Arrange
        ToolCallResponse response1 = ToolCallResponse.success("result");
        ToolCallResponse response2 = ToolCallResponse.success("result");

        // Assert
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void toolCallResponse_ShouldHandleStringResult() {
        // Act
        ToolCallResponse response = ToolCallResponse.success("Simple string result");

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isEqualTo("Simple string result");
    }

    @Test
    void toolCallResponse_ShouldHandleNumericResult() {
        // Act
        ToolCallResponse response = ToolCallResponse.success(42);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isEqualTo(42);
    }

    @Test
    void toolCallResponse_ShouldHandleArrayResult() {
        // Arrange
        java.util.List<String> arrayResult = java.util.List.of("item1", "item2", "item3");

        // Act
        ToolCallResponse response = ToolCallResponse.success(arrayResult);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isInstanceOf(java.util.List.class);
    }

    @Test
    void toolCallResponse_ErrorAndSuccessResponsesShouldDiffer() {
        // Arrange
        ToolCallResponse success = ToolCallResponse.success("data");
        ToolCallResponse error = ToolCallResponse.error("error message");

        // Assert
        assertThat(success).isNotEqualTo(error);
        assertThat(success.success()).isTrue();
        assertThat(error.success()).isFalse();
    }

    @Test
    void toolCallResponse_ShouldHandleMutableMaps() {
        // Arrange
        Map<String, Object> mutableMap = new HashMap<>();
        mutableMap.put("key1", "value1");
        mutableMap.put("key2", "value2");

        // Act
        ToolCallResponse response = ToolCallResponse.success(mutableMap);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isInstanceOf(Map.class);
    }
}

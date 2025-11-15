package com.cuius.mcpclient.controller;

import com.cuius.mcpclient.model.ToolCallRequest;
import com.cuius.mcpclient.model.ToolCallResponse;
import com.cuius.mcpclient.model.ToolInfo;
import com.cuius.mcpclient.service.McpToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for McpController.
 * Tests REST API endpoints using MockMvc without starting a full server.
 */
@WebMvcTest(McpController.class)
class McpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private McpToolService mcpToolService;

    @Test
    void initialize_ShouldReturnOk_WhenInitializationSucceeds() throws Exception {
        // Arrange
        doNothing().when(mcpToolService).initialize();

        // Act & Assert
        mockMvc.perform(post("/api/mcp/initialize"))
                .andExpect(status().isOk())
                .andExpect(content().string("MCP client initialized successfully"));

        verify(mcpToolService, times(1)).initialize();
    }

    @Test
    void initialize_ShouldReturnInternalServerError_WhenInitializationFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Connection timeout"))
                .when(mcpToolService).initialize();

        // Act & Assert
        mockMvc.perform(post("/api/mcp/initialize"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed to initialize MCP client")))
                .andExpect(content().string(containsString("Connection timeout")));

        verify(mcpToolService, times(1)).initialize();
    }

    @Test
    void listTools_ShouldReturnToolsList_WhenToolsAvailable() throws Exception {
        // Arrange
        List<ToolInfo> tools = List.of(
                new ToolInfo("get_crypto_price", "Get cryptocurrency price", Map.of("symbol", Map.of("type", "string"))),
                new ToolInfo("get_market_cap", "Get market capitalization", Map.of("symbol", Map.of("type", "string")))
        );

        when(mcpToolService.listTools()).thenReturn(tools);

        // Act & Assert
        mockMvc.perform(get("/api/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("get_crypto_price"))
                .andExpect(jsonPath("$[0].description").value("Get cryptocurrency price"))
                .andExpect(jsonPath("$[1].name").value("get_market_cap"))
                .andExpect(jsonPath("$[1].description").value("Get market capitalization"));

        verify(mcpToolService, times(1)).listTools();
    }

    @Test
    void listTools_ShouldReturnEmptyList_WhenNoToolsAvailable() throws Exception {
        // Arrange
        when(mcpToolService.listTools()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(mcpToolService, times(1)).listTools();
    }

    @Test
    void listTools_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(mcpToolService.listTools())
                .thenThrow(new RuntimeException("Server connection lost"));

        // Act & Assert
        mockMvc.perform(get("/api/mcp/tools"))
                .andExpect(status().isInternalServerError());

        verify(mcpToolService, times(1)).listTools();
    }

    @Test
    void callTool_ShouldReturnOk_WhenToolCallSucceeds() throws Exception {
        // Arrange
        ToolCallRequest request = new ToolCallRequest("get_crypto_price", Map.of("symbol", "BTC"));
        ToolCallResponse successResponse = ToolCallResponse.success(Map.of("price", 50000.00));

        when(mcpToolService.callTool(any(ToolCallRequest.class))).thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.price").value(50000.00))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(mcpToolService, times(1)).callTool(any(ToolCallRequest.class));
    }

    @Test
    void callTool_ShouldReturnBadRequest_WhenToolCallFails() throws Exception {
        // Arrange
        ToolCallRequest request = new ToolCallRequest("invalid_tool", Map.of());
        ToolCallResponse errorResponse = ToolCallResponse.error("Tool not found");

        when(mcpToolService.callTool(any(ToolCallRequest.class))).thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Tool not found"))
                .andExpect(jsonPath("$.result").doesNotExist());

        verify(mcpToolService, times(1)).callTool(any(ToolCallRequest.class));
    }

    @Test
    void callTool_ShouldHandleComplexToolArguments() throws Exception {
        // Arrange
        Map<String, Object> complexArgs = Map.of(
                "symbol", "ETH",
                "currency", "USD",
                "includeHistory", true,
                "limit", 10
        );
        ToolCallRequest request = new ToolCallRequest("get_crypto_data", complexArgs);
        ToolCallResponse successResponse = ToolCallResponse.success(Map.of("data", "complex result"));

        when(mcpToolService.callTool(any(ToolCallRequest.class))).thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(mcpToolService, times(1)).callTool(any(ToolCallRequest.class));
    }

    @Test
    void callTool_ShouldReturnBadRequest_WhenRequestBodyIsMalformed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"invalid json\""))
                .andExpect(status().isBadRequest());

        verify(mcpToolService, never()).callTool(any(ToolCallRequest.class));
    }

    @Test
    void callTool_ShouldHandleNullArguments() throws Exception {
        // Arrange
        String requestJson = "{\"toolName\":\"simple_tool\",\"arguments\":null}";
        ToolCallResponse successResponse = ToolCallResponse.success("result");

        when(mcpToolService.callTool(any(ToolCallRequest.class))).thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(mcpToolService, times(1)).callTool(any(ToolCallRequest.class));
    }

    @Test
    void close_ShouldReturnOk_WhenCloseSucceeds() throws Exception {
        // Arrange
        doNothing().when(mcpToolService).close();

        // Act & Assert
        mockMvc.perform(post("/api/mcp/close"))
                .andExpect(status().isOk())
                .andExpect(content().string("MCP client closed successfully"));

        verify(mcpToolService, times(1)).close();
    }

    @Test
    void close_ShouldReturnInternalServerError_WhenCloseFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Failed to close connection"))
                .when(mcpToolService).close();

        // Act & Assert
        mockMvc.perform(post("/api/mcp/close"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed to close MCP client")))
                .andExpect(content().string(containsString("Failed to close connection")));

        verify(mcpToolService, times(1)).close();
    }

    @Test
    void endpoints_ShouldUseCorrectHttpMethods() throws Exception {
        // Initialize endpoint - POST only
        mockMvc.perform(get("/api/mcp/initialize"))
                .andExpect(status().isMethodNotAllowed());

        // List tools endpoint - GET only
        mockMvc.perform(post("/api/mcp/tools"))
                .andExpect(status().isMethodNotAllowed());

        // Call tool endpoint - POST only
        mockMvc.perform(get("/api/mcp/tools/call"))
                .andExpect(status().isMethodNotAllowed());

        // Close endpoint - POST only
        mockMvc.perform(get("/api/mcp/close"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void callTool_ShouldAcceptOnlyJsonContentType() throws Exception {
        // Arrange
        ToolCallRequest request = new ToolCallRequest("test_tool", Map.of());

        // Act & Assert - Should fail with plain text content type
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void listTools_ShouldHandleToolsWithComplexInputSchemas() throws Exception {
        // Arrange
        Map<String, Object> complexSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "symbol", Map.of("type", "string", "required", true),
                        "options", Map.of("type", "object", "properties", Map.of(
                                "includeHistory", Map.of("type", "boolean"),
                                "limit", Map.of("type", "number")
                        ))
                )
        );

        List<ToolInfo> tools = List.of(
                new ToolInfo("complex_tool", "A tool with complex schema", complexSchema)
        );

        when(mcpToolService.listTools()).thenReturn(tools);

        // Act & Assert
        mockMvc.perform(get("/api/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("complex_tool"))
                .andExpect(jsonPath("$[0].inputSchema.type").value("object"))
                .andExpect(jsonPath("$[0].inputSchema.properties").exists());
    }
}

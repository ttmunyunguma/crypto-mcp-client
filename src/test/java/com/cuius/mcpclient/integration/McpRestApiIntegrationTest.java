package com.cuius.mcpclient.integration;

import com.cuius.mcpclient.config.TestMcpClientConfig;
import com.cuius.mcpclient.model.ToolCallRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for REST API endpoints.
 * Tests the complete HTTP request/response cycle with mocked MCP server.
 *
 * Note: These tests are disabled because MCP SDK uses final classes that cannot be fully mocked.
 * For true integration tests, use a real MCP server with Testcontainers.
 * See TESTING.md for setup instructions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMcpClientConfig.class)
@org.junit.jupiter.api.Disabled("MCP SDK final classes prevent proper mocking - requires real MCP server")
class McpRestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private McpSyncClient mcpClient;

    @BeforeEach
    void setUp() {
        reset(mcpClient);
    }

    @Test
    void initialize_Endpoint_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(mcpClient).initialize();

        // Act & Assert
        mockMvc.perform(post("/api/mcp/initialize"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("MCP client initialized successfully"));

        verify(mcpClient, times(1)).initialize();
    }

    @Test
    void initialize_Endpoint_ShouldReturnError_WhenInitializationFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Server not available"))
                .when(mcpClient).initialize();

        // Act & Assert
        mockMvc.perform(post("/api/mcp/initialize"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed to initialize MCP client")))
                .andExpect(content().string(containsString("Server not available")));
    }

    @Test
    void listTools_Endpoint_ShouldReturnToolsArray() throws Exception {
        // Arrange
        McpSchema.Tool tool1 = createMockTool("get_price", "Get crypto price");
        McpSchema.Tool tool2 = createMockTool("get_volume", "Get trading volume");

        McpSchema.ListToolsResult listToolsResult = mock(McpSchema.ListToolsResult.class);
        when(listToolsResult.tools()).thenReturn(List.of(tool1, tool2));
        when(mcpClient.listTools()).thenReturn(listToolsResult);

        // Act & Assert
        mockMvc.perform(get("/api/mcp/tools")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("get_price")))
                .andExpect(jsonPath("$[0].description", is("Get crypto price")))
                .andExpect(jsonPath("$[1].name", is("get_volume")))
                .andExpect(jsonPath("$[1].description", is("Get trading volume")));

        verify(mcpClient, times(1)).listTools();
    }

    @Test
    void listTools_Endpoint_ShouldReturnEmptyArray_WhenNoTools() throws Exception {
        // Arrange
        McpSchema.ListToolsResult listToolsResult = mock(McpSchema.ListToolsResult.class);
        when(listToolsResult.tools()).thenReturn(List.of());
        when(mcpClient.listTools()).thenReturn(listToolsResult);

        // Act & Assert
        mockMvc.perform(get("/api/mcp/tools"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void callTool_Endpoint_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        ToolCallRequest request = new ToolCallRequest("get_price",
                Map.of("symbol", "BTC", "currency", "USD"));

        McpSchema.Content content = mock(McpSchema.Content.class);
        List<McpSchema.Content> contentList = List.of(content);

        McpSchema.CallToolResult callResult = mock(McpSchema.CallToolResult.class);
        when(callResult.isError()).thenReturn(false);
        when(callResult.content()).thenReturn(contentList);

        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(callResult);

        // Act & Assert
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(mcpClient, times(1)).callTool(any(McpSchema.CallToolRequest.class));
    }

    @Test
    void callTool_Endpoint_ShouldReturnErrorResponse_WhenToolFails() throws Exception {
        // Arrange
        ToolCallRequest request = new ToolCallRequest("invalid_tool", Map.of());

        McpSchema.Content errorContent = mock(McpSchema.Content.class);
        when(errorContent.toString()).thenReturn("Tool 'invalid_tool' not found");

        McpSchema.CallToolResult callResult = mock(McpSchema.CallToolResult.class);
        when(callResult.isError()).thenReturn(true);
        when(callResult.content()).thenReturn(List.of(errorContent));

        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(callResult);

        // Act & Assert
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", containsString("not found")))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    void callTool_Endpoint_ShouldValidateRequestBody() throws Exception {
        // Act & Assert - Invalid JSON
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"invalid json\""))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(mcpClient, never()).callTool(any(McpSchema.CallToolRequest.class));
    }

    @Test
    void close_Endpoint_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(mcpClient).closeGracefully();

        // Act & Assert
        mockMvc.perform(post("/api/mcp/close"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("MCP client closed successfully"));

        verify(mcpClient, times(1)).closeGracefully();
    }

    @Test
    void close_Endpoint_ShouldReturnError_WhenCloseFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Already closed"))
                .when(mcpClient).closeGracefully();

        // Act & Assert
        mockMvc.perform(post("/api/mcp/close"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed to close MCP client")));
    }

    @Test
    void completeWorkflow_ShouldWorkEndToEnd() throws Exception {
        // 1. Initialize
        doNothing().when(mcpClient).initialize();
        mockMvc.perform(post("/api/mcp/initialize"))
                .andExpect(status().isOk());

        // 2. List Tools
        McpSchema.Tool tool = createMockTool("test_tool", "Test tool");
        McpSchema.ListToolsResult listToolsResult = mock(McpSchema.ListToolsResult.class);
        when(listToolsResult.tools()).thenReturn(List.of(tool));
        when(mcpClient.listTools()).thenReturn(listToolsResult);

        mockMvc.perform(get("/api/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("test_tool")));

        // 3. Call Tool
        McpSchema.Content content = mock(McpSchema.Content.class);
        McpSchema.CallToolResult callResult = mock(McpSchema.CallToolResult.class);
        when(callResult.isError()).thenReturn(false);
        when(callResult.content()).thenReturn(List.of(content));
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(callResult);

        ToolCallRequest request = new ToolCallRequest("test_tool", Map.of("param", "value"));
        mockMvc.perform(post("/api/mcp/tools/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 4. Close
        doNothing().when(mcpClient).closeGracefully();
        mockMvc.perform(post("/api/mcp/close"))
                .andExpect(status().isOk());

        // Verify all interactions
        verify(mcpClient, times(1)).initialize();
        verify(mcpClient, times(1)).listTools();
        verify(mcpClient, times(1)).callTool(any(McpSchema.CallToolRequest.class));
        verify(mcpClient, times(1)).closeGracefully();
    }

    @Test
    void httpMethods_ShouldBeCorrectlyEnforced() throws Exception {
        // Initialize - only POST allowed
        mockMvc.perform(get("/api/mcp/initialize"))
                .andExpect(status().isMethodNotAllowed());

        // List tools - only GET allowed
        mockMvc.perform(post("/api/mcp/tools"))
                .andExpect(status().isMethodNotAllowed());

        // Call tool - only POST allowed
        mockMvc.perform(get("/api/mcp/tools/call"))
                .andExpect(status().isMethodNotAllowed());

        // Close - only POST allowed
        mockMvc.perform(get("/api/mcp/close"))
                .andExpect(status().isMethodNotAllowed());
    }

    // Helper methods
    private McpSchema.Tool createMockTool(String name, String description) {
        McpSchema.Tool tool = mock(McpSchema.Tool.class);
        when(tool.name()).thenReturn(name);
        when(tool.description()).thenReturn(description);

        // Mock the input schema with a properties() method
        when(tool.inputSchema()).thenAnswer(invocation -> {
            return new Object() {
                public Map<String, Object> properties() {
                    return Map.of(
                            "param1", Map.of("type", "string"),
                            "param2", Map.of("type", "number")
                    );
                }
            };
        });

        return tool;
    }
}

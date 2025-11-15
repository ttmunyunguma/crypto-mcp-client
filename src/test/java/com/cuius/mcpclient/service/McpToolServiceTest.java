package com.cuius.mcpclient.service;

import com.cuius.mcpclient.model.ToolCallRequest;
import com.cuius.mcpclient.model.ToolCallResponse;
import com.cuius.mcpclient.model.ToolInfo;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for McpToolService.
 * Tests the service layer's interaction with the MCP client and proper error handling.
 */
@ExtendWith(MockitoExtension.class)
class McpToolServiceTest {

    @Mock
    private McpSyncClient mcpClient;

    @InjectMocks
    private McpToolService mcpToolService;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    @Test
    void initialize_ShouldSuccessfullyInitializeClient() {
        // Arrange
        doNothing().when(mcpClient).initialize();

        // Act
        mcpToolService.initialize();

        // Assert
        verify(mcpClient, times(1)).initialize();
    }

    @Test
    void initialize_ShouldThrowRuntimeException_WhenClientInitializationFails() {
        // Arrange
        doThrow(new RuntimeException("Connection failed"))
                .when(mcpClient).initialize();

        // Act & Assert
        assertThatThrownBy(() -> mcpToolService.initialize())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to initialize MCP client")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(mcpClient, times(1)).initialize();
    }

    @Test
    void listTools_ShouldReturnListOfTools_WhenServerRespondsSuccessfully() {
        // Arrange
        McpSchema.Tool tool1 = createMockTool("get_crypto_price", "Get current cryptocurrency price");
        McpSchema.Tool tool2 = createMockTool("get_market_cap", "Get market capitalization");

        McpSchema.ListToolsResult listToolsResult = mock(McpSchema.ListToolsResult.class);
        when(listToolsResult.tools()).thenReturn(List.of(tool1, tool2));
        when(mcpClient.listTools()).thenReturn(listToolsResult);

        // Act
        List<ToolInfo> tools = mcpToolService.listTools();

        // Assert
        assertThat(tools)
                .hasSize(2)
                .extracting(ToolInfo::name)
                .containsExactly("get_crypto_price", "get_market_cap");

        assertThat(tools)
                .extracting(ToolInfo::description)
                .containsExactly("Get current cryptocurrency price", "Get market capitalization");

        verify(mcpClient, times(1)).listTools();
    }

    @Test
    void listTools_ShouldReturnEmptyList_WhenNoToolsAvailable() {
        // Arrange
        McpSchema.ListToolsResult listToolsResult = mock(McpSchema.ListToolsResult.class);
        when(listToolsResult.tools()).thenReturn(List.of());
        when(mcpClient.listTools()).thenReturn(listToolsResult);

        // Act
        List<ToolInfo> tools = mcpToolService.listTools();

        // Assert
        assertThat(tools).isEmpty();
        verify(mcpClient, times(1)).listTools();
    }

    @Test
    void listTools_ShouldThrowRuntimeException_WhenClientThrowsException() {
        // Arrange
        when(mcpClient.listTools())
                .thenThrow(new RuntimeException("Server connection lost"));

        // Act & Assert
        assertThatThrownBy(() -> mcpToolService.listTools())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to list tools from MCP server")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(mcpClient, times(1)).listTools();
    }

    @Test
    void listTools_ShouldHandleNullInputSchema() {
        // Arrange
        McpSchema.Tool tool = mock(McpSchema.Tool.class);
        when(tool.name()).thenReturn("simple_tool");
        when(tool.description()).thenReturn("A simple tool");
        when(tool.inputSchema()).thenReturn(null);

        McpSchema.ListToolsResult listToolsResult = mock(McpSchema.ListToolsResult.class);
        when(listToolsResult.tools()).thenReturn(List.of(tool));
        when(mcpClient.listTools()).thenReturn(listToolsResult);

        // Act
        List<ToolInfo> tools = mcpToolService.listTools();

        // Assert
        assertThat(tools).hasSize(1);
        assertThat(tools.get(0).inputSchema()).isEmpty();
    }

    @Test
    void callTool_ShouldReturnSuccessResponse_WhenToolExecutesSuccessfully() {
        // Arrange
        String toolName = "get_crypto_price";
        Map<String, Object> arguments = Map.of("symbol", "BTC", "currency", "USD");
        ToolCallRequest request = new ToolCallRequest(toolName, arguments);

        McpSchema.Content content = mock(McpSchema.Content.class);
        List<McpSchema.Content> contentList = List.of(content);

        McpSchema.CallToolResult result = mock(McpSchema.CallToolResult.class);
        when(result.isError()).thenReturn(false);
        when(result.content()).thenReturn(contentList);

        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

        // Act
        ToolCallResponse response = mcpToolService.callTool(request);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isNotNull();
        assertThat(response.error()).isNull();

        ArgumentCaptor<McpSchema.CallToolRequest> captor =
                ArgumentCaptor.forClass(McpSchema.CallToolRequest.class);
        verify(mcpClient, times(1)).callTool(captor.capture());

        McpSchema.CallToolRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.name()).isEqualTo(toolName);
        assertThat(capturedRequest.arguments()).isEqualTo(arguments);
    }

    @Test
    void callTool_ShouldReturnErrorResponse_WhenToolReturnsError() {
        // Arrange
        String toolName = "invalid_tool";
        Map<String, Object> arguments = Map.of("param", "value");
        ToolCallRequest request = new ToolCallRequest(toolName, arguments);

        McpSchema.Content errorContent = mock(McpSchema.Content.class);
        when(errorContent.toString()).thenReturn("Tool not found");
        List<McpSchema.Content> contentList = List.of(errorContent);

        McpSchema.CallToolResult result = mock(McpSchema.CallToolResult.class);
        when(result.isError()).thenReturn(true);
        when(result.content()).thenReturn(contentList);

        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

        // Act
        ToolCallResponse response = mcpToolService.callTool(request);

        // Assert
        assertThat(response.success()).isFalse();
        assertThat(response.result()).isNull();
        assertThat(response.error()).contains("Tool not found");

        verify(mcpClient, times(1)).callTool(any(McpSchema.CallToolRequest.class));
    }

    @Test
    void callTool_ShouldReturnErrorResponse_WhenExceptionOccurs() {
        // Arrange
        String toolName = "failing_tool";
        Map<String, Object> arguments = Map.of();
        ToolCallRequest request = new ToolCallRequest(toolName, arguments);

        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenThrow(new RuntimeException("Network timeout"));

        // Act
        ToolCallResponse response = mcpToolService.callTool(request);

        // Assert
        assertThat(response.success()).isFalse();
        assertThat(response.result()).isNull();
        assertThat(response.error()).contains("Failed to call tool");
        assertThat(response.error()).contains("Network timeout");

        verify(mcpClient, times(1)).callTool(any(McpSchema.CallToolRequest.class));
    }

    @Test
    void callTool_ShouldHandleNullIsErrorField() {
        // Arrange
        String toolName = "ambiguous_tool";
        Map<String, Object> arguments = Map.of("key", "value");
        ToolCallRequest request = new ToolCallRequest(toolName, arguments);

        McpSchema.Content content = mock(McpSchema.Content.class);
        List<McpSchema.Content> contentList = List.of(content);

        McpSchema.CallToolResult result = mock(McpSchema.CallToolResult.class);
        when(result.isError()).thenReturn(null); // Null isError field
        when(result.content()).thenReturn(contentList);

        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

        // Act
        ToolCallResponse response = mcpToolService.callTool(request);

        // Assert
        assertThat(response.success()).isTrue();
        assertThat(response.result()).isNotNull();
    }

    @Test
    void close_ShouldCloseClientGracefully() {
        // Arrange
        doNothing().when(mcpClient).closeGracefully();

        // Act
        mcpToolService.close();

        // Assert
        verify(mcpClient, times(1)).closeGracefully();
    }

    @Test
    void close_ShouldNotThrowException_WhenCloseGracefullyFails() {
        // Arrange
        doThrow(new RuntimeException("Connection already closed"))
                .when(mcpClient).closeGracefully();

        // Act - should not throw
        mcpToolService.close();

        // Assert
        verify(mcpClient, times(1)).closeGracefully();
    }

    @Test
    void callTool_ShouldHandleEmptyArguments() {
        // Arrange
        String toolName = "no_arg_tool";
        Map<String, Object> emptyArguments = Map.of();
        ToolCallRequest request = new ToolCallRequest(toolName, emptyArguments);

        McpSchema.Content content = mock(McpSchema.Content.class);
        List<McpSchema.Content> contentList = List.of(content);

        McpSchema.CallToolResult result = mock(McpSchema.CallToolResult.class);
        when(result.isError()).thenReturn(false);
        when(result.content()).thenReturn(contentList);

        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

        // Act
        ToolCallResponse response = mcpToolService.callTool(request);

        // Assert
        assertThat(response.success()).isTrue();
        verify(mcpClient, times(1)).callTool(any(McpSchema.CallToolRequest.class));
    }

    @Test
    void callTool_ShouldHandleComplexArgumentTypes() {
        // Arrange
        String toolName = "complex_tool";
        Map<String, Object> complexArguments = Map.of(
                "stringParam", "value",
                "numberParam", 42,
                "booleanParam", true,
                "arrayParam", List.of(1, 2, 3),
                "objectParam", Map.of("nested", "data")
        );
        ToolCallRequest request = new ToolCallRequest(toolName, complexArguments);

        McpSchema.Content content = mock(McpSchema.Content.class);
        List<McpSchema.Content> contentList = List.of(content);

        McpSchema.CallToolResult result = mock(McpSchema.CallToolResult.class);
        when(result.isError()).thenReturn(false);
        when(result.content()).thenReturn(contentList);

        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(result);

        // Act
        ToolCallResponse response = mcpToolService.callTool(request);

        // Assert
        assertThat(response.success()).isTrue();
        ArgumentCaptor<McpSchema.CallToolRequest> captor =
                ArgumentCaptor.forClass(McpSchema.CallToolRequest.class);
        verify(mcpClient).callTool(captor.capture());
        assertThat(captor.getValue().arguments()).isEqualTo(complexArguments);
    }

    // Helper method to create mock tools
    private McpSchema.Tool createMockTool(String name, String description) {
        McpSchema.Tool tool = mock(McpSchema.Tool.class);
        when(tool.name()).thenReturn(name);
        when(tool.description()).thenReturn(description);

        // Mock the input schema - it should have a properties() method
        Object inputSchema = mock(Object.class);
        // Use a workaround since we can't mock the exact type without knowing the interface
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

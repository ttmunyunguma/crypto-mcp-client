package com.cuius.mcpclient.runner;

import com.cuius.mcpclient.model.ToolInfo;
import com.cuius.mcpclient.service.McpToolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Unit tests for McpClientRunner.
 * Tests the application startup runner that initializes and demonstrates MCP client functionality.
 */
@ExtendWith(MockitoExtension.class)
class McpClientRunnerTest {

    @Mock
    private McpToolService mcpToolService;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private McpClientRunner mcpClientRunner;

    private List<ToolInfo> sampleTools;

    @BeforeEach
    void setUp() {
        sampleTools = List.of(
                new ToolInfo("get_crypto_price", "Get cryptocurrency price",
                        Map.of("symbol", Map.of("type", "string"))),
                new ToolInfo("get_market_cap", "Get market capitalization",
                        Map.of("symbol", Map.of("type", "string")))
        );
    }

    @Test
    void run_ShouldInitializeAndListTools_WhenSuccessful() throws Exception {
        // Arrange
        doNothing().when(mcpToolService).initialize();
        when(mcpToolService.listTools()).thenReturn(sampleTools);

        // Act
        mcpClientRunner.run(applicationArguments);

        // Assert
        verify(mcpToolService, times(1)).initialize();
        verify(mcpToolService, times(1)).listTools();
    }

    @Test
    void run_ShouldHandleInitializationFailure() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Connection failed"))
                .when(mcpToolService).initialize();

        // Act - should not throw, just log the error
        mcpClientRunner.run(applicationArguments);

        // Assert
        verify(mcpToolService, times(1)).initialize();
        verify(mcpToolService, never()).listTools();
    }

    @Test
    void run_ShouldHandleListToolsFailure() throws Exception {
        // Arrange
        doNothing().when(mcpToolService).initialize();
        when(mcpToolService.listTools())
                .thenThrow(new RuntimeException("Failed to fetch tools"));

        // Act - should not throw, just log the error
        mcpClientRunner.run(applicationArguments);

        // Assert
        verify(mcpToolService, times(1)).initialize();
        verify(mcpToolService, times(1)).listTools();
    }

    @Test
    void run_ShouldHandleEmptyToolsList() throws Exception {
        // Arrange
        doNothing().when(mcpToolService).initialize();
        when(mcpToolService.listTools()).thenReturn(List.of());

        // Act
        mcpClientRunner.run(applicationArguments);

        // Assert
        verify(mcpToolService, times(1)).initialize();
        verify(mcpToolService, times(1)).listTools();
    }

    @Test
    void run_ShouldHandleToolsWithNullFields() throws Exception {
        // Arrange
        List<ToolInfo> toolsWithNullFields = List.of(
                new ToolInfo("tool1", null, Map.of()),
                new ToolInfo("tool2", "description", null)
        );

        doNothing().when(mcpToolService).initialize();
        when(mcpToolService.listTools()).thenReturn(toolsWithNullFields);

        // Act - should handle null fields gracefully
        mcpClientRunner.run(applicationArguments);

        // Assert
        verify(mcpToolService, times(1)).initialize();
        verify(mcpToolService, times(1)).listTools();
    }

    @Test
    void run_ShouldExecuteInCorrectOrder() throws Exception {
        // Arrange
        doNothing().when(mcpToolService).initialize();
        when(mcpToolService.listTools()).thenReturn(sampleTools);

        // Act
        mcpClientRunner.run(applicationArguments);

        // Assert - verify order of operations
        var inOrder = inOrder(mcpToolService);
        inOrder.verify(mcpToolService).initialize();
        inOrder.verify(mcpToolService).listTools();
    }

    @Test
    void run_ShouldHandleMultipleTools() throws Exception {
        // Arrange
        List<ToolInfo> manyTools = List.of(
                new ToolInfo("tool1", "desc1", Map.of("param1", Map.of("type", "string"))),
                new ToolInfo("tool2", "desc2", Map.of("param2", Map.of("type", "number"))),
                new ToolInfo("tool3", "desc3", Map.of("param3", Map.of("type", "boolean"))),
                new ToolInfo("tool4", "desc4", Map.of("param4", Map.of("type", "array"))),
                new ToolInfo("tool5", "desc5", Map.of("param5", Map.of("type", "object")))
        );

        doNothing().when(mcpToolService).initialize();
        when(mcpToolService.listTools()).thenReturn(manyTools);

        // Act
        mcpClientRunner.run(applicationArguments);

        // Assert
        verify(mcpToolService, times(1)).initialize();
        verify(mcpToolService, times(1)).listTools();
    }

    @Test
    void run_ShouldHandleToolsWithComplexSchemas() throws Exception {
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

        List<ToolInfo> complexTools = List.of(
                new ToolInfo("complex_tool", "Complex tool with nested schema", complexSchema)
        );

        doNothing().when(mcpToolService).initialize();
        when(mcpToolService.listTools()).thenReturn(complexTools);

        // Act
        mcpClientRunner.run(applicationArguments);

        // Assert
        verify(mcpToolService, times(1)).initialize();
        verify(mcpToolService, times(1)).listTools();
    }
}

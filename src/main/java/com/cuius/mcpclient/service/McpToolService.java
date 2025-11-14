package com.cuius.mcpclient.service;

import com.cuius.mcpclient.model.ToolCallRequest;
import com.cuius.mcpclient.model.ToolCallResponse;
import com.cuius.mcpclient.model.ToolInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class McpToolService {

    private static final Logger logger = LoggerFactory.getLogger(McpToolService.class);

    private final McpClient mcpClient;

    public McpToolService(McpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    /**
     * Initialize the MCP client connection
     */
    public void initialize() {
        try {
            logger.info("Initializing MCP client...");
            mcpClient.initialize();
            logger.info("MCP client initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize MCP client", e);
            throw new RuntimeException("Failed to initialize MCP client", e);
        }
    }

    /**
     * Get all available tools from the MCP server
     */
    public List<ToolInfo> listTools() {
        try {
            logger.info("Fetching tools from MCP server...");
            McpSchema.ListToolsResult toolsResult = mcpClient.listTools();

            List<ToolInfo> tools = toolsResult.tools().stream()
                    .map(tool -> new ToolInfo(
                            tool.name(),
                            tool.description(),
                            tool.inputSchema() != null ? tool.inputSchema().properties() : Map.of()
                    ))
                    .collect(Collectors.toList());

            logger.info("Retrieved {} tools from MCP server", tools.size());
            return tools;
        } catch (Exception e) {
            logger.error("Failed to list tools", e);
            throw new RuntimeException("Failed to list tools from MCP server", e);
        }
    }

    /**
     * Call a tool on the MCP server
     */
    public ToolCallResponse callTool(ToolCallRequest request) {
        try {
            logger.info("Calling tool '{}' with arguments: {}", request.toolName(), request.arguments());

            McpSchema.CallToolResult result = mcpClient.callTool(
                    new McpSchema.CallToolRequest(request.toolName(), request.arguments())
            );

            if (result.isError() != null && result.isError()) {
                logger.error("Tool call failed: {}", result.content());
                return ToolCallResponse.error(result.content().toString());
            }

            logger.info("Tool call successful: {}", result.content());
            return ToolCallResponse.success(result.content());
        } catch (Exception e) {
            logger.error("Failed to call tool '{}'", request.toolName(), e);
            return ToolCallResponse.error("Failed to call tool: " + e.getMessage());
        }
    }

    /**
     * Close the MCP client connection
     */
    public void close() {
        try {
            logger.info("Closing MCP client...");
            mcpClient.closeGracefully();
            logger.info("MCP client closed successfully");
        } catch (Exception e) {
            logger.error("Failed to close MCP client", e);
        }
    }
}

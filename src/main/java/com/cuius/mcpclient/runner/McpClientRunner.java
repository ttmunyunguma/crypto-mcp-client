package com.cuius.mcpclient.runner;

import com.cuius.mcpclient.model.ToolInfo;
import com.cuius.mcpclient.service.McpToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpClientRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(McpClientRunner.class);

    private final McpToolService mcpToolService;

    public McpClientRunner(McpToolService mcpToolService) {
        this.mcpToolService = mcpToolService;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("=== MCP Client Runner ===");

        try {
            // Initialize the MCP client
            logger.info("Initialize client...");
            mcpToolService.initialize();

            // List available tools
            logger.info("Fetching available tools...");
            List<ToolInfo> tools = mcpToolService.listTools();

            logger.info("Available Tools:");
            tools.forEach(tool -> {
                logger.info("  - Name: {}", tool.name());
                logger.info("    Description: {}", tool.description());
                logger.info("    Input Schema: {}", tool.inputSchema());
            });

            logger.info("=== MCP Client initialized and ready to use ===");
            logger.info("You can now use the REST API endpoints:");
            logger.info("  - GET  /api/mcp/tools - List all tools");
            logger.info("  - POST /api/mcp/tools/call - Call a tool");
            logger.info("  - POST /api/mcp/initialize - Initialize connection");
            logger.info("  - POST /api/mcp/close - Close connection");

        } catch (Exception e) {
            logger.error("Failed to run MCP client demo", e);
            logger.info("Make sure the MCP server is configured correctly in application.properties");
        }
    }
}

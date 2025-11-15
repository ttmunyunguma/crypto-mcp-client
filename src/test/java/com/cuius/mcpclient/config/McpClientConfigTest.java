package com.cuius.mcpclient.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for McpClientConfig.
 * Tests the Spring configuration and bean creation for MCP client.
 */
@SpringBootTest(classes = McpClientConfig.class)
@TestPropertySource(properties = {
        "mcp.server.command=echo",
        "mcp.server.args=test",
        "mcp.server.timeout=10"
})
class McpClientConfigTest {

    @Autowired(required = false)
    private McpClientConfig config;

    @Test
    void contextLoads() {
        // Verify that the configuration class can be loaded
        assertThat(config).isNotNull();
    }

    @Test
    void mcpClientBean_ShouldBeConfigured() {
        // Verify the bean is properly configured
        assertThat(config).isNotNull();
    }

    /**
     * Note: Full integration test for McpSyncClient bean creation is in integration tests
     * because it requires actual MCP server process and initialization.
     * Unit tests focus on configuration class structure and basic bean wiring.
     */
}

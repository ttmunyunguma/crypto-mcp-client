package com.cuius.mcpclient.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for configuration in test profile.
 * Tests that the test configuration properly provides mocked beans.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestMcpClientConfig.class)
class McpClientConfigTest {

    @Autowired
    private McpSyncClient mcpClient;

    @Test
    void contextLoads() {
        // Verify that the test context loads properly
        assertThat(mcpClient).isNotNull();
    }

    @Test
    void mcpClientBean_ShouldBeConfigured() {
        // Verify the mocked bean is properly configured in test profile
        assertThat(mcpClient).isNotNull();
    }

    /**
     * Note: Tests for production McpClientConfig require an actual MCP server.
     * These tests verify that the test profile provides proper mocked beans.
     * Full integration test for real McpSyncClient is in integration tests with real server.
     */
}

package com.cuius.mcpclient.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that provides mock beans for integration tests.
 * This allows tests to run without requiring an actual MCP server.
 */
@TestConfiguration
@Profile("test")
public class TestMcpClientConfig {

    /**
     * Provides a mock McpSyncClient for testing without real MCP server.
     * Tests that need real client behavior should use @SpringBootTest with actual configuration.
     */
    @Bean
    @Primary
    public McpSyncClient mockMcpClient() {
        return mock(McpSyncClient.class);
    }
}

package com.cuius.mcpclient.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class McpClientConfig {

    @Value("${mcp.server.command:java}")
    private String serverCommand;

    @Value("${mcp.server.args:-jar}")
    private String[] serverArgs;

    @Value("${mcp.server.timeout:30}")
    private long timeoutSeconds;

    @Bean(destroyMethod = "closeGracefully")
    public McpSyncClient mcpClient() {
        ServerParameters serverParams = ServerParameters.builder(serverCommand)
                .args(serverArgs)
                .build();

        StdioClientTransport transport = new StdioClientTransport(serverParams);

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        client.initialize();

        return client;
    }
}

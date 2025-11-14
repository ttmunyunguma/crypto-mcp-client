package com.cuius.mcpclient.config;

import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.stdio.ServerParameters;
import org.springframework.ai.mcp.client.stdio.StdioServerMcpTransport;
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

    @Bean
    public McpClient mcpClient() {
        ServerParameters serverParams = ServerParameters.builder(serverCommand)
                .args(serverArgs)
                .build();

        StdioServerMcpTransport transport = new StdioServerMcpTransport(serverParams);

        return McpClient.sync(transport, Duration.ofSeconds(timeoutSeconds));
    }
}

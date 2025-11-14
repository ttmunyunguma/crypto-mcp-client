# Crypto MCP Client

A Spring Boot application that implements an MCP (Model Context Protocol) client using Spring AI. This client connects to an MCP server to discover and execute tools dynamically.

## Features

- **MCP Protocol Support**: Full implementation of the Model Context Protocol for tool discovery and execution
- **Spring AI Integration**: Leverages Spring AI's MCP client capabilities
- **REST API**: HTTP endpoints for easy integration and testing
- **Tool Discovery**: Automatically discovers available tools from the MCP server
- **Tool Execution**: Execute tools with dynamic arguments
- **Logging**: Comprehensive logging for debugging and monitoring

## Architecture

The application consists of several key components:

- **McpClientConfig**: Configures the MCP client connection to the server
- **McpToolService**: Core service for interacting with the MCP server
- **McpController**: REST API endpoints for client operations
- **McpClientRunner**: Startup demo to showcase available tools

## Prerequisites

- Java 21 or higher
- Gradle
- Running MCP server (e.g., spring-mcp-server)

## Configuration

Update `src/main/resources/application.properties` with your MCP server details:

```properties
# MCP Server Configuration
mcp.server.command=java
mcp.server.args=-jar,/path/to/spring-mcp-server.jar
mcp.server.timeout=30
```

### Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `mcp.server.command` | Command to start the MCP server | `java` |
| `mcp.server.args` | Arguments for the server command | `-jar` |
| `mcp.server.timeout` | Connection timeout in seconds | `30` |
| `server.port` | HTTP server port | `8080` |

## Building the Application

```bash
./gradlew build
```

## Running the Application

```bash
./gradlew bootRun
```

Or run the JAR directly:

```bash
java -jar build/libs/crypto-mcp-client-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Initialize MCP Client

```bash
POST http://localhost:8080/api/mcp/initialize
```

Initializes the connection to the MCP server.

### List Available Tools

```bash
GET http://localhost:8080/api/mcp/tools
```

Returns a list of all available tools from the MCP server.

**Response Example:**
```json
[
  {
    "name": "encrypt",
    "description": "Encrypts data using specified algorithm",
    "inputSchema": {
      "data": "string",
      "algorithm": "string"
    }
  }
]
```

### Call a Tool

```bash
POST http://localhost:8080/api/mcp/tools/call
Content-Type: application/json

{
  "toolName": "encrypt",
  "arguments": {
    "data": "Hello, World!",
    "algorithm": "AES"
  }
}
```

**Response Example:**
```json
{
  "success": true,
  "result": "encrypted_data_here",
  "error": null
}
```

### Close MCP Client

```bash
POST http://localhost:8080/api/mcp/close
```

Gracefully closes the MCP client connection.

## Usage Example

### Using cURL

1. Initialize the client:
```bash
curl -X POST http://localhost:8080/api/mcp/initialize
```

2. List available tools:
```bash
curl http://localhost:8080/api/mcp/tools
```

3. Call a tool:
```bash
curl -X POST http://localhost:8080/api/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "your-tool-name",
    "arguments": {
      "param1": "value1",
      "param2": "value2"
    }
  }'
```

### Using Java/Spring

```java
@Autowired
private McpToolService mcpToolService;

public void example() {
    // Initialize
    mcpToolService.initialize();

    // List tools
    List<ToolInfo> tools = mcpToolService.listTools();

    // Call a tool
    ToolCallRequest request = new ToolCallRequest(
        "toolName",
        Map.of("arg1", "value1")
    );
    ToolCallResponse response = mcpToolService.callTool(request);
}
```

## Project Structure

```
src/main/java/com/cuius/mcpclient/
├── config/
│   └── McpClientConfig.java          # MCP client configuration
├── controller/
│   └── McpController.java            # REST API endpoints
├── model/
│   ├── ToolInfo.java                 # Tool information model
│   ├── ToolCallRequest.java          # Tool call request model
│   └── ToolCallResponse.java         # Tool call response model
├── runner/
│   └── McpClientRunner.java          # Startup demo runner
├── service/
│   └── McpToolService.java           # Core MCP service
└── CryptoMcpClientApplication.java   # Main application class
```

## Logging

The application includes comprehensive logging. To adjust log levels, modify `application.properties`:

```properties
logging.level.com.cuius.mcpclient=INFO
logging.level.org.springframework.ai.mcp=DEBUG
```

## Error Handling

The client includes robust error handling:

- Connection errors are logged and thrown as RuntimeExceptions
- Tool execution errors return a ToolCallResponse with success=false
- All errors include descriptive messages for debugging

## Development

### Adding New Features

1. Extend `McpToolService` for additional MCP operations
2. Add new endpoints in `McpController` as needed
3. Update models in the `model` package for new data structures

### Testing

Run tests with:

```bash
./gradlew test
```

## Troubleshooting

### Connection Issues

- Verify the MCP server is running and accessible
- Check the `mcp.server.command` and `mcp.server.args` configuration
- Review logs for detailed error messages

### Tool Execution Failures

- Ensure tool arguments match the expected schema
- Check the MCP server logs for server-side errors
- Verify the tool name is correct (case-sensitive)

## License

This project is part of the Cuius platform.

## Related Projects

- **spring-mcp-server**: The MCP server implementation this client connects to

# MCP Client Testing Guide

This document provides comprehensive information about the test suite for the crypto-mcp-client application.

## Test Structure

The test suite is organized into three main categories:

### 1. Unit Tests
Located in `src/test/java/com/cuius/mcpclient/`

#### Model Tests (`model/ModelClassesTest.java`)
- **Purpose**: Test the record classes (ToolInfo, ToolCallRequest, ToolCallResponse)
- **Coverage**:
  - JSON serialization/deserialization
  - Record equality and hashCode
  - Null handling
  - Complex data structures
  - Static factory methods

#### Service Tests (`service/McpToolServiceTest.java`)
- **Purpose**: Test the McpToolService business logic
- **Coverage**:
  - Client initialization
  - Tool listing
  - Tool invocation
  - Error handling
  - Connection lifecycle management
- **Mocking Strategy**: Uses Mockito to mock McpSyncClient

#### Controller Tests (`controller/McpControllerTest.java`)
- **Purpose**: Test REST API endpoints using MockMvc
- **Coverage**:
  - HTTP method validation
  - Request/response marshalling
  - Error responses
  - Content-type validation
  - Parameter validation
- **Framework**: Spring WebMvcTest with MockMvc

#### Runner Tests (`runner/McpClientRunnerTest.java`)
- **Purpose**: Test the ApplicationRunner that initializes the client on startup
- **Coverage**:
  - Successful initialization flow
  - Error handling during startup
  - Tool listing on startup
  - Logging verification

### 2. Integration Tests
Located in `src/test/java/com/cuius/mcpclient/integration/`

#### MCP Client Integration Tests (`McpClientIntegrationTest.java`)
- **Purpose**: Test end-to-end MCP client interactions
- **Coverage**:
  - Complete workflow (initialize → list → call → close)
  - Multiple sequential tool calls
  - Error propagation
  - Large dataset handling
  - Connection lifecycle
- **Configuration**: Uses test profile with mocked McpSyncClient

#### REST API Integration Tests (`McpRestApiIntegrationTest.java`)
- **Purpose**: Test complete HTTP request/response cycles
- **Coverage**:
  - All REST endpoints
  - Complete workflows
  - HTTP method enforcement
  - Content negotiation
  - Error scenarios
- **Framework**: SpringBootTest with AutoConfigureMockMvc

### 3. Configuration Tests
Located in `src/test/java/com/cuius/mcpclient/config/`

#### Configuration Tests (`McpClientConfigTest.java`)
- **Purpose**: Test Spring configuration and bean wiring
- **Coverage**:
  - Bean creation
  - Configuration validation
  - Property binding

## Test Configuration

### Test Properties
Located in `src/test/resources/application-test.properties`

```properties
spring.application.name=crypto-mcp-client-test
server.port=0  # Random port for testing
mcp.server.command=echo  # Mock command for tests
mcp.server.args=test
mcp.server.timeout=5
logging.level.com.cuius.mcpclient=DEBUG
```

### Test Configuration Class
`TestMcpClientConfig.java` provides:
- Mock McpSyncClient bean for testing
- Test-specific configuration
- Profile-based activation

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "com.cuius.mcpclient.service.McpToolServiceTest"
```

### Run Specific Test Method
```bash
./gradlew test --tests "com.cuius.mcpclient.service.McpToolServiceTest.initialize_ShouldSuccessfullyInitializeClient"
```

### Run Only Unit Tests
```bash
./gradlew test --tests "com.cuius.mcpclient.*Test"
```

### Run Only Integration Tests
```bash
./gradlew test --tests "com.cuius.mcpclient.integration.*"
```

### Generate Test Reports
```bash
./gradlew test
# View report at: build/reports/tests/test/index.html
```

### Run Tests with Coverage
```bash
./gradlew test jacocoTestReport
# View coverage at: build/reports/jacoco/test/html/index.html
```

## Test Dependencies

The following test dependencies are configured in `build.gradle`:

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.mockito:mockito-core'
testImplementation 'org.mockito:mockito-junit-jupiter'
testImplementation 'org.assertj:assertj-core'
testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'
testImplementation 'org.awaitility:awaitility:4.2.0'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

## Key Test Scenarios Covered

### 1. Connection Lifecycle
- ✅ Successful initialization
- ✅ Initialization failures
- ✅ Graceful shutdown
- ✅ Error handling on close

### 2. Tool Management
- ✅ Listing available tools
- ✅ Empty tool list
- ✅ Tools with complex schemas
- ✅ Null field handling

### 3. Tool Execution
- ✅ Successful tool calls
- ✅ Tool execution errors
- ✅ Invalid tool names
- ✅ Empty arguments
- ✅ Complex argument types
- ✅ Network timeouts

### 4. REST API
- ✅ All HTTP endpoints
- ✅ Request validation
- ✅ Response formatting
- ✅ Error responses (4xx, 5xx)
- ✅ Content-type validation
- ✅ HTTP method enforcement

### 5. Error Handling
- ✅ Connection failures
- ✅ Server errors
- ✅ Timeout scenarios
- ✅ Invalid parameters
- ✅ Null handling

### 6. Data Handling
- ✅ JSON serialization
- ✅ Large payloads
- ✅ Complex nested structures
- ✅ Null and empty values

## Testing Best Practices

### Unit Tests
1. **Isolation**: Each test should be independent and not rely on others
2. **Mocking**: Use Mockito to mock external dependencies
3. **AAA Pattern**: Arrange, Act, Assert
4. **Clear Names**: Test names should describe what they test
5. **Single Responsibility**: Each test should verify one behavior

### Integration Tests
1. **Test Profiles**: Use `@ActiveProfiles("test")` for test-specific configuration
2. **Mock External Services**: Don't rely on actual MCP servers
3. **Transaction Management**: Use `@Transactional` where appropriate
4. **Clean State**: Reset mocks in `@BeforeEach`

### General Guidelines
1. **Fast Execution**: Tests should run quickly
2. **Deterministic**: Tests should produce consistent results
3. **Readable**: Tests should be easy to understand
4. **Maintainable**: Tests should be easy to update
5. **Comprehensive**: Aim for high code coverage

## Common Issues and Solutions

### Issue: Tests fail with "Connection refused"
**Solution**: Ensure you're using the test profile which provides a mock MCP client

### Issue: Mockito exceptions
**Solution**: The MCP SDK uses final classes. Use test configuration to provide pre-configured mocks

### Issue: Random port conflicts
**Solution**: Use `server.port=0` in test properties to get a random available port

### Issue: Tests timeout
**Solution**: Adjust timeout in test properties: `mcp.server.timeout=30`

## Future Enhancements

### Recommended Additions
1. **Performance Tests**: Add JMH benchmarks for critical paths
2. **Contract Tests**: Use Spring Cloud Contract for API testing
3. **Mutation Testing**: Add PIT for mutation coverage
4. **Security Tests**: Add OWASP dependency check
5. **Load Tests**: Add Gatling or JMeter scenarios
6. **Real MCP Server Tests**: Add tests with actual MCP server instances
7. **Testcontainers**: Use Docker containers for integration tests

### Code Coverage Goals
- **Unit Tests**: > 80% line coverage
- **Integration Tests**: > 60% line coverage
- **Overall**: > 75% line coverage

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run tests
        run: ./gradlew test
      - name: Publish Test Report
        uses: mikepenz/action-junit-report@v2
        if: always()
        with:
          report_paths: '**/build/test-results/test/TEST-*.xml'
```

## Troubleshooting

### Enable Debug Logging
Add to `application-test.properties`:
```properties
logging.level.com.cuius.mcpclient=DEBUG
logging.level.org.springframework.test=DEBUG
```

### Run Single Test with Debug
```bash
./gradlew test --tests "TestClassName.testMethod" --debug
```

### View Detailed Failure Information
```bash
./gradlew test --info
```

## References

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)

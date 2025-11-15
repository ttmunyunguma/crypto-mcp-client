package com.cuius.mcpclient.controller;

import com.cuius.mcpclient.model.ToolCallRequest;
import com.cuius.mcpclient.model.ToolCallResponse;
import com.cuius.mcpclient.model.ToolInfo;
import com.cuius.mcpclient.service.McpToolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private final McpToolService mcpToolService;

    public McpController(McpToolService mcpToolService) {
        this.mcpToolService = mcpToolService;
    }

    /**
     * Initialize the MCP client connection
     */
    @PostMapping("/initialize")
    public ResponseEntity<String> initialize() {
        try {
            mcpToolService.initialize();
            return ResponseEntity.ok("MCP client initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to initialize MCP client: " + e.getMessage());
        }
    }

    /**
     * Get all available tools from the MCP server
     */
    @GetMapping("/tools")
    public ResponseEntity<List<ToolInfo>> listTools() {
        try {
            List<ToolInfo> tools = mcpToolService.listTools();
            return ResponseEntity.ok(tools);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Call a specific tool
     */
    @PostMapping("/tools/call")
    public ResponseEntity<ToolCallResponse> callTool(@RequestBody ToolCallRequest request) {
        ToolCallResponse response = mcpToolService.callTool(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Close the MCP client connection
     */
    @PostMapping("/close")
    public ResponseEntity<String> close() {
        try {
            mcpToolService.close();
            return ResponseEntity.ok("MCP client closed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to close MCP client: " + e.getMessage());
        }
    }
}

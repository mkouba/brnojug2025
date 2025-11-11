package org.acme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ToolsTest {

    @Test
    public void testAnswer() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
                .toolsCall("theAnswer", Map.of("lang", "Java"), r -> {
                    assertEquals("Spaces are better for indentation.", r.content().get(0).asText().text());
                })
                .toolsCall("theAnswer", Map.of("lang", "python"), r -> {
                    assertEquals("Tabs are better for indentation.", r.content().get(0).asText().text());
                })
                .thenAssertResults();
    }

    @Test
    public void testListMonsters() {
        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();

        client.when()
                .toolsCall("listMonsters", Map.of("minimalHitPoints", 50), toolResponse -> {
                    assertTrue(toolResponse.content().get(0).asText().text().contains("Chimera"));
                })
                .thenAssertResults();
    }

}

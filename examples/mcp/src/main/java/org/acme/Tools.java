package org.acme;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Sampling;
import io.quarkiverse.mcp.server.SamplingMessage;
import io.quarkiverse.mcp.server.SamplingRequest;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.PositiveOrZero;

public class Tools {

    @Tool(description = """
            Answer the ultimate question to tabs vs. spaces
            """)
    String theAnswer(@ToolArg(description = "The programming language", defaultValue = "Java") String lang,
            McpLog log) {
        log.info("Let's try to answer the question for lang: %s", lang);
        if ("python".equalsIgnoreCase(lang)) {
            return "Tabs are better for indentation.";
        }
        return "Spaces are better for indentation.";
    }

    record MonsterResult(List<Monster> monsters) {
    }

    @WithTransaction
    @Tool(description = """
            List the D&D monsters.
            """)
    Uni<MonsterResult> listMonsters(
            @ToolArg(description = "Minimal number of hit points", defaultValue = "1") @PositiveOrZero int minimalHitPoints) {
        return Monster.<Monster> list("where hitPoints >= :minimalHitPoints",
                Parameters.with("minimalHitPoints", minimalHitPoints)).map(list -> new MonsterResult(list));
    }

    record MonsterAndDescription(Monster monster, String desciption) {
    }

    @Tool(description = "Returns a random fantasy monster with description.")
    Uni<MonsterAndDescription> randomMonster(Sampling sampling) {
        if (sampling.isSupported()) {
            return Panache.withSession(() -> Monster.<Monster> listAll()).chain(all -> {
                int index = ThreadLocalRandom.current().nextInt(all.size());
                Monster monster = all.get(index);
                SamplingRequest samplingRequest = sampling.requestBuilder().setMaxTokens(100)
                        .addMessage(SamplingMessage.withUserRole("Give me a description of " + monster.name))
                        .build();
                return samplingRequest.send()
                        .map(response -> new MonsterAndDescription(monster, response.content().asText().text()));
            });
        } else {
            throw new ToolCallException("Sampling not supported");
        }
    }

}

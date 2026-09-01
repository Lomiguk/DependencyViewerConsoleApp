package ru.dsckibin.util.visualization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.dsckibin.hierarchy.Dependency;
import ru.dsckibin.hierarchy.Node;
import ru.dsckibin.hierarchy.TypeOfDependency;
import ru.dsckibin.util.ClassNameUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphvizToolTest {
    @TempDir
    Path directory;

    private final GraphvizTool tool = new GraphvizTool(
            "graph", new GraphvizDataMapper(new ClassNameUtil())
    );

    @Test
    void createsACompleteDotDocument() {
        var dependency = new Dependency();
        dependency.putNew(TypeOfDependency.INVOKE);
        var node = new Node("example.Service", true)
                .addDependencies(Map.of("example.Repository", dependency));

        var dot = tool.buildDot(Set.of(node), false, List.of(), false);

        assertTrue(dot.startsWith("digraph G {"));
        assertTrue(dot.contains("\"example.Service\" -> \"example.Repository\""));
        assertTrue(dot.endsWith("}\n"));
    }

    @Test
    void supportsWildcardsAnywhereInIgnoreExpressions() {
        var dot = tool.buildDot(
                Set.of(new Node("example.generated.Service", true)),
                false,
                List.of("*.generated.*"),
                false
        );

        assertFalse(dot.contains("example.generated.Service"));
    }

    @Test
    void rendersSvgAndPngWithoutAnInstalledDotCommand() throws IOException {
        var prefix = directory.resolve("dependency-graph");
        var renderer = new GraphvizTool(
                prefix.toString(), new GraphvizDataMapper(new ClassNameUtil())
        );

        renderer.drawGraph(
                Set.of(new Node("example.Service", true)),
                false,
                List.of(),
                false
        );

        assertTrue(Files.readString(Path.of(prefix + ".svg")).contains("<svg"));
        var png = Files.readAllBytes(Path.of(prefix + ".png"));
        assertTrue(png.length > 8);
        assertTrue(png[0] == (byte) 0x89 && png[1] == 'P' && png[2] == 'N' && png[3] == 'G');
    }
}

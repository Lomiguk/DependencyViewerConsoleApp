package ru.dsckibin.util.visualization;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.parse.Parser;
import ru.dsckibin.hierarchy.Node;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

public class GraphvizTool {
    private static boolean engineConfigured;

    private final String filePrefix;
    private final GraphvizDataMapper dataMapper;

    public GraphvizTool(String filePrefix, GraphvizDataMapper dataMapper) {
        this.filePrefix = filePrefix;
        this.dataMapper = dataMapper;
        configureEngine();
    }

    public void drawGraph(
            Set<Node> hierarchy,
            boolean useGitDiff,
            Collection<String> ignoredNames,
            boolean simplifyNames
    ) {
        var dotPath = Path.of(filePrefix + ".dot");
        var svgPath = Path.of(filePrefix + ".svg");
        var imagePath = Path.of(filePrefix + ".png");

        try {
            var dot = buildDot(hierarchy, useGitDiff, ignoredNames, simplifyNames);
            Files.writeString(dotPath, dot, StandardCharsets.UTF_8);

            var graph = new Parser().read(dot);
            Graphviz.fromGraph(graph).render(Format.SVG_STANDALONE).toFile(svgPath.toFile());
            Graphviz.fromGraph(graph).render(Format.PNG).toFile(imagePath.toFile());

            System.out.printf(
                    "Graphs saved to %s and %s%n",
                    svgPath.toAbsolutePath(),
                    imagePath.toAbsolutePath()
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render the dependency graph", e);
        }
    }

    private static synchronized void configureEngine() {
        if (!engineConfigured) {
            Graphviz.useEngine(new GraphvizV8Engine(), new GraphvizCmdLineEngine());
            engineConfigured = true;
        }
    }

    String buildDot(
            Set<Node> hierarchy,
            boolean useGitDiff,
            Collection<String> ignoredNames,
            boolean simplifyNames
    ) {
        var result = new StringBuilder("digraph G {\n");
        result.append(GraphvizDataHandler.STYLE);

        for (var node : hierarchy) {
            if (isIgnored(ignoredNames, node.getName())) {
                continue;
            }
            if (!useGitDiff || node.getChangedStatus()) {
                result.append(dataMapper.mapJarClass(node, simplifyNames));
                node.getDependencies().forEach((className, dependency) -> {
                    if (!isIgnored(ignoredNames, className)) {
                        result.append(dataMapper.mapDependencyToEdgeString(
                                node, className, dependency, simplifyNames
                        ));
                    }
                });
            }
        }
        return result.append("}\n").toString();
    }

    private boolean isIgnored(Collection<String> ignoredNames, String name) {
        for (var expression : ignoredNames) {
            var regex = "^" + Pattern.quote(expression).replace("*", "\\E.*\\Q") + "$";
            if (name.matches(regex)) {
                return true;
            }
        }
        return false;
    }
}

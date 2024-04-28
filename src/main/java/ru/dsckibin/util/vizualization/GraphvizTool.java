package ru.dsckibin.util.vizualization;

import ru.dsckibin.hierarchy.GitView;
import ru.dsckibin.hierarchy.Node;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class GraphvizTool {
    private final String filePrefix;
    private final StringBuilder stringBuilder;
    private final GraphvizDataMapper dataMapper;


    public GraphvizTool(
            String filePrefix,
            StringBuilder stringBuilder,
            GraphvizDataMapper dataMapper
    ) {
        this.filePrefix = filePrefix;
        this.stringBuilder = stringBuilder;
        this.dataMapper = dataMapper;
    }

    public void drawGraph(
            Set<Node> hierarchy,
            Boolean useGitDif,
            Collection<String> ignoredNames,
            Boolean simplifyNames
    ) {
        initDotFileData();
        implementEdges(hierarchy, useGitDif, ignoredNames, simplifyNames);
        digraphWrapping();
        saveDotFile();
    }

    private void initDotFileData() {
        stringBuilder.append(GrapvizDataHandler.STYLE);
    }

    private void implementEdges(
            Set<Node> hierarchy,
            Boolean useGitDif,
            Collection<String> ignoredNames,
            Boolean simplifyNames
    ) {
        for (var node : hierarchy) {
            if (isIgnored(ignoredNames, node.getName())) continue;
            var gitStatus = node.getGitView();
            if (!useGitDif || gitStatus.equals(GitView.CHANGED) || gitStatus.equals(GitView.INTERVAL)) {
                stringBuilder.append(
                        dataMapper.mapJarClass(node, simplifyNames)
                );
                node.getDependencies().forEach((depNode, dependency) -> {
                    if (!isIgnored(ignoredNames, depNode.getName())) {
                        stringBuilder.append(
                                dataMapper.mapDependencyToEdgeString(
                                        node,
                                        depNode.getName(),
                                        dependency,
                                        simplifyNames
                                )
                        );
                    }
                });
            }
        }
    }

    private Boolean isIgnored(Collection<String> ignoredName, String name) {
        for (var bannedClass : ignoredName) {
            if (bannedClass.startsWith("*") && name.endsWith(bannedClass.substring(1))) {
                return true;
            }
            if (bannedClass.endsWith("*") && name.startsWith(bannedClass.substring(0, bannedClass.length() - 1))) {
                return true;
            }
            if (bannedClass.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void digraphWrapping() {
        stringBuilder.insert(0, "digraph G {\n");
        stringBuilder.append("}\n");
    }

    private void saveDotFile() {
        try (var outputStream = new FileOutputStream(filePrefix + ".dot")) {
            outputStream.write(stringBuilder.toString().getBytes());

            Runtime.getRuntime().exec(
                    String.format(GrapvizDataHandler.SAVE_COMMAND_FORMAT, filePrefix, filePrefix)
            );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

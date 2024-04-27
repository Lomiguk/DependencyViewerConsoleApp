package ru.dsckibin.util.vizualization;

import ru.dsckibin.hierarchy.Node;
import ru.dsckibin.util.ClassNameUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class GraphvizTool {
    private final String filePrefix;
    private final StringBuilder stringBuilder;
    private final GraphvizDataMapper dataMapper;
    private final ClassNameUtil classNameUtil;


    public GraphvizTool(
            String filePrefix,
            StringBuilder stringBuilder,
            GraphvizDataMapper dataMapper,
            ClassNameUtil classNameUtil
    ) {
        this.filePrefix = filePrefix;
        this.stringBuilder = stringBuilder;
        this.dataMapper = dataMapper;
        this.classNameUtil = classNameUtil;
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
            if ((!useGitDif || node.getChangedStatus())) {
                stringBuilder.append(
                        dataMapper.mapJarClass(node, simplifyNames)
                );
                node.getDependencies().forEach((className, dependency) -> {
                    if (!isIgnored(ignoredNames, className)) {
                        stringBuilder.append(
                                dataMapper.mapDependencyToEdgeString(
                                        node,
                                        className,
                                        dependency,
                                        simplifyNames
                                )
                        );
                    }
                });
            }
        }
        hierarchy.forEach(node -> {

        });
    }

    private Boolean isIgnored(Collection<String> ignoredName, String name) {
        for (var bannedClass : ignoredName) {
            if (bannedClass.startsWith("*") && name.endsWith(bannedClass.substring(1))) {
                return true;
            }
            if (bannedClass.endsWith("*") && name.startsWith(bannedClass.substring(0, bannedClass.length()-1))) {
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

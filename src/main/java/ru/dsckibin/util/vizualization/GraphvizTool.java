package ru.dsckibin.util.vizualization;

import ru.dsckibin.hierarchy.Node;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public void drawGraph(Set<Node> hierarchy) {
        initDotFileData();
        implementEdge(hierarchy);
        digraphWrapping();
        saveDotFile();
    }

    private void initDotFileData() {
        stringBuilder.append(GrapvizDataHandler.STYLE);
    }

    private void implementEdge(Set<Node> hierarchy) {
        hierarchy.forEach(node -> {
            node.getDependencies().forEach((className, dependency) -> {
                stringBuilder.append(
                        dataMapper.mapDependencyToEdgeString(node, className, dependency)
                );
            });
        });
    }

    private void digraphWrapping() {
        stringBuilder.insert(0, "digraph G {" + "\n");
        stringBuilder.append("}").append("\n");
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

package ru.dsckibin.util.visualization;

public final class GraphvizDataHandler {
    private GraphvizDataHandler() {
    }
    public final static String STYLE = """
                                 fontname="Helvetica,Arial,sans-serif"
                                 node [fontname="Helvetica,Arial,sans-serif"];
                                 edge [fontname="Helvetica,Arial,sans-serif"];
                                 node [shape=box];
                                 rankdir="LR";
                                 subgraph cluster_L {
                                  "Legend: " [shape=box fontsize=16 label="Green - new; Red - invoke; blue - field; black - method parameter" tooltip="[stackcollapse]"]
                                 }
                        """;
}

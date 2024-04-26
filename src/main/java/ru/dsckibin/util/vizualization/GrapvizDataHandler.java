package ru.dsckibin.util.vizualization;

public class GrapvizDataHandler {
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

    public final static String SAVE_COMMAND_FORMAT = "dot -Tpng %s.dot -o %s.png";
}

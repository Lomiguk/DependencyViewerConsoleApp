package ru.dsckibin.util.vizualization;

import ru.dsckibin.hierarchy.DependencyNode;
import ru.dsckibin.hierarchy.Node;

public class GraphvizDataMapper {
    private final static String EDGE_FORMAT = "   \"%s\" -> \"%s\"";
    private final static String SETTINGS_START = " [";
    private final static String SETTINGS_END = "]\n";
    private final static String COLOR_SETTING = "color = ";
    private final static String FIELD_COLOR = "blue";
    private final static String INVOKE_COLOR = "red";
    private final static String NEW_COLOR = "green";
    private final static String METHOD_PARAM_COLOR = "black";
    private final static String LABEL_SETTING_FORMAT = ", label = %d";

    public String mapDependencyToEdgeString(
            Node parent,
            DependencyNode dependencyNode,
            Integer weight
    ) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(EDGE_FORMAT+SETTINGS_START, parent.getName(), dependencyNode.getName()));
        switch (dependencyNode.getTypeOfDependency()) {
            case FIELD -> stringBuilder.append(COLOR_SETTING + FIELD_COLOR);
            case INVOKE -> stringBuilder.append(COLOR_SETTING + INVOKE_COLOR);
            case NEW -> stringBuilder.append(COLOR_SETTING + NEW_COLOR);
            case METHOD_PARAM -> stringBuilder.append(COLOR_SETTING + METHOD_PARAM_COLOR);
        }
        stringBuilder.append(String.format(LABEL_SETTING_FORMAT, weight));
        stringBuilder.append(SETTINGS_END);

        return stringBuilder.toString();
    }
}

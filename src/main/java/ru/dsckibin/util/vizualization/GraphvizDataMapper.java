package ru.dsckibin.util.vizualization;

import ru.dsckibin.hierarchy.Dependency;
import ru.dsckibin.hierarchy.Node;

import java.util.stream.Collectors;

public class GraphvizDataMapper {
    private final static String EDGE_FORMAT = "   \"%s\" -> \"%s\"\n";
    private final static String SETTINGS_START = " [";
    private final static String SETTINGS_END = "]\n";
    private final static String COLOR_SETTING_FORMAT = "color = \"%s\"";
    private final static String FIELD_COLOR = "#0000ff";
    private final static String INVOKE_COLOR = "#ff0000";
    private final static String NEW_COLOR = "#00ff00";
    private final static String METHOD_PARAM_COLOR = "#000000";
    private final static String LABEL_SETTING_FORMAT = "label = \"%s\"";

    /*public String mapDependencyToEdgeString(
            Node parent,
            DependencyNode dependencyNode,
            Integer weight
    ) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(EDGE_FORMAT + SETTINGS_START, parent.getName(), dependencyNode.getName()));
        switch (dependencyNode.getTypeOfDependency()) {
            case FIELD -> stringBuilder.append(COLOR_SETTING + FIELD_COLOR);
            case INVOKE -> stringBuilder.append(COLOR_SETTING + INVOKE_COLOR);
            case NEW -> stringBuilder.append(COLOR_SETTING + NEW_COLOR);
            case METHOD_PARAM -> stringBuilder.append(COLOR_SETTING + METHOD_PARAM_COLOR);
        }
        stringBuilder.append(String.format(LABEL_SETTING_FORMAT, weight));
        stringBuilder.append(SETTINGS_END);

        return stringBuilder.toString();
    }*/

    public String mapDependencyToEdgeString(Node node, String depName, Dependency dependency) {
        var stringBuilder = new StringBuilder();
        if (dependency.isEmpty()) {
            return stringBuilder.toString();
        }

        stringBuilder.append(String.format("{ edge [%s %s]\n", buildColorProperty(dependency), buildLabelProperty(dependency)));
        stringBuilder.append(String.format(EDGE_FORMAT, node.getName(), depName));
        stringBuilder.append("}\n");
        return stringBuilder.toString();
    }

    private String buildColorProperty(Dependency dependency) {
        return String.format(COLOR_SETTING_FORMAT, dependenciesToColors(dependency));
    }

    private String buildLabelProperty(Dependency dependency) {
        return String.format(LABEL_SETTING_FORMAT, dependenciesToCountLabel(dependency));
    }

    private String dependenciesToColors(Dependency dependency) {
        return String.join(":", dependency.keySet().stream()
                .map(key -> {
                    String color;
                    switch (key) {
                        case NEW -> color = NEW_COLOR;
                        case INVOKE -> color = INVOKE_COLOR;
                        case FIELD -> color = FIELD_COLOR;
                        case METHOD_PARAM -> color = METHOD_PARAM_COLOR;
                        default -> color = "";
                    }
                    return color;
                })
                .collect(Collectors.toSet()));
    }

    private String dependenciesToCountLabel(Dependency dependency) {
        var depCount = new DepCount();

        dependency.forEach( (type, weight) -> {
            switch (type) {
                case NEW -> depCount.setNewDep(weight);
                case INVOKE -> depCount.setInvokeDep(weight);
                case FIELD -> depCount.setFieldDep(weight);
                case METHOD_PARAM -> depCount.setMethodDep(weight);
            }
        });

        return depCount.toString();
    }

    private static class DepCount {
        Integer newDep = 0;
        Integer invokeDep = 0;
        Integer fieldDep = 0;
        Integer methodDep = 0;

        public void setFieldDep(Integer fieldDep) {
            this.fieldDep = fieldDep;
        }

        public void setNewDep(Integer newDep) {
            this.newDep = newDep;
        }
        public void setInvokeDep(Integer invokeDep) {
            this.invokeDep = invokeDep;
        }

        public void setMethodDep(Integer methodDep) {
            this.methodDep = methodDep;
        }
        @Override
        public String toString() {
            return String.format("%d;%d;%d;%d", newDep, invokeDep, fieldDep, methodDep);
        }
    }
}

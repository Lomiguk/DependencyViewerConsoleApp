package ru.dsckibin.util.visualization;

import ru.dsckibin.hierarchy.Dependency;
import ru.dsckibin.hierarchy.Node;
import ru.dsckibin.hierarchy.TypeOfDependency;
import ru.dsckibin.util.ClassNameUtil;

import java.util.List;

public class GraphvizDataMapper {
    private final static String EDGE_CLUSTER_FORMAT = """
            { edge [%s %s]
                "%s" -> "%s"
            }
            """;
    private final static String COLOR_SETTING_FORMAT = "color = \"%s\"";
    private final static String FIELD_COLOR = "#0000ff";
    private final static String INVOKE_COLOR = "#ff0000";
    private final static String NEW_COLOR = "#00ff00";
    private final static String METHOD_PARAM_COLOR = "#000000";
    private final static String JAR_CLASS_COLOR = "#ff0000";
    private final static String LABEL_SETTING_FORMAT = "label = \"%s\"";
    private final static String DEPENDENCY_COUNT_FORMAT = "%d;%d;%d;%d";
    private final static String JAR_CLASS_FORMAT = "\"%s\" [color = \"%s\"]\n";
    private final static String COLOR_SPLITTER = ":";
    private final static String EMPTY_STRING = "";

    private final ClassNameUtil classNameUtil;

    public GraphvizDataMapper(ClassNameUtil classNameUtil) {
        this.classNameUtil = classNameUtil;
    }


    public String mapDependencyToEdgeString(
            Node node,
            String depName,
            Dependency dependency,
            boolean simplifyNames
    ) {
        var stringBuilder = new StringBuilder();
        if (dependency.isEmpty()) {
            return stringBuilder.toString();
        }

        stringBuilder.append(getEdgeClusterStart(
                dependency,
                simplifyNames ? classNameUtil.simplifyName(node.getName()) : node.getName(),
                simplifyNames ? classNameUtil.simplifyName(depName) : depName
        ));

        return stringBuilder.toString();
    }

    private String getEdgeClusterStart(Dependency dependency, String nodeName, String depName) {
        return String.format(
                EDGE_CLUSTER_FORMAT,
                buildColorProperty(dependency),
                buildLabelProperty(dependency),
                nodeName,
                depName
        );
    }

    private String buildColorProperty(Dependency dependency) {
        return String.format(COLOR_SETTING_FORMAT, dependenciesToColors(dependency));
    }

    private String buildLabelProperty(Dependency dependency) {
        return String.format(LABEL_SETTING_FORMAT, dependenciesToCountLabel(dependency));
    }

    private String dependenciesToColors(Dependency dependency) {
        return String.join(COLOR_SPLITTER, List.of(
                        TypeOfDependency.NEW,
                        TypeOfDependency.INVOKE,
                        TypeOfDependency.FIELD,
                        TypeOfDependency.METHOD_PARAM
                ).stream()
                .filter(dependency::containsKey)
                .map(key -> {
                    String color;
                    switch (key) {
                        case NEW -> color = NEW_COLOR;
                        case INVOKE -> color = INVOKE_COLOR;
                        case FIELD -> color = FIELD_COLOR;
                        case METHOD_PARAM -> color = METHOD_PARAM_COLOR;
                        default -> color = EMPTY_STRING;
                    }
                    return color;
                })
                .toList());
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

    public String mapJarClass(Node node, boolean simplifyName) {
        return String.format(
                JAR_CLASS_FORMAT,
                simplifyName ? classNameUtil.simplifyName(node.getName()) : node.getName(),
                JAR_CLASS_COLOR
        );
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
            return String.format(DEPENDENCY_COUNT_FORMAT, newDep, invokeDep, fieldDep, methodDep);
        }
    }
}

package ru.dsckibin.hierarchy;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private final String name;
    private final Boolean isChanged;
    private final Map<Node, Dependency> dependencies = new HashMap<>();

    public Node(String name) {
        this.name = name;
        this.isChanged = false;
    }

    public String getName() {
        return name;
    }

    public Boolean getChangedStatus() {
        return isChanged;
    }

    public Map<Node, Dependency> getDependencies() {
        return dependencies;
    }

    public Node(String name, Boolean isChanged) {
        this.name = name;
        this.isChanged = isChanged;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Node)) {
            return false;
        } else if (this.name.equals(((Node) obj).name)){
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public Node addDependencies(Map<Node, Dependency> newPairs) {
        newPairs.forEach( (node, dependencyTypes) -> {
            if (dependencies.containsKey(node)) {
                dependencyTypes.forEach( (depType, weight) -> {
                    var pairDepTypeWeight = dependencies.get(node);
                    if (pairDepTypeWeight.containsKey(depType)) {
                        pairDepTypeWeight.put(depType, pairDepTypeWeight.get(depType) + weight);
                    } else {
                        pairDepTypeWeight.put(depType, weight);
                    }
                });
            } else {
                dependencies.put(node, dependencyTypes);
            }
        });
        return this;
    }
}

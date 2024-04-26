package ru.dsckibin.hierarchy;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private final String name;
    private final Boolean isChanged;
    private final Map<String, Dependency> dependencies = new HashMap<>();

    public Node(String name) {
        this.name = name;
        this.isChanged = false;
    }

    public String getName() {
        return name;
    }

    public Boolean getChanged() {
        return isChanged;
    }

    public Map<String, Dependency> getDependencies() {
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

    public Node addDependencies(Map<String, Dependency> newPairs) {
        newPairs.forEach((depName, dependencyTypes) -> {
            if (dependencies.containsKey(depName)) {
                dependencyTypes.forEach( (depType, weight) -> {
                    var pairDepTypeWeight = dependencies.get(depName);
                    if (pairDepTypeWeight.containsKey(depType)) {
                        pairDepTypeWeight.put(depType, pairDepTypeWeight.get(depType) + weight);
                    } else {
                        pairDepTypeWeight.put(depType, weight);
                    }
                });
            } else {
                dependencies.put(depName, dependencyTypes);
            }
        });
        return this;
    }
}

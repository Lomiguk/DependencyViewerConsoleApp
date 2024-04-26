package ru.dsckibin.hierarchy;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private final String name;
    private final Boolean isChanged;
    private final Map<DependencyNode, Integer> dependencies = new HashMap<>();

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

    public Map<DependencyNode, Integer> getDependencies() {
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

    public Node addDependencies(Map<DependencyNode, Integer> newPairs) {
        dependencies.putAll(newPairs);
        return this;
    }
}

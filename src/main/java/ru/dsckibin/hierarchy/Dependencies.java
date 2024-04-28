package ru.dsckibin.hierarchy;

import java.util.HashMap;

public class Dependencies extends HashMap<Node, Dependency> {
    public Node getNode(String searchableName) {
        for (var node : this.keySet()) {
            if (node.getName().equals(searchableName)) {
                return node;
            }
            var searchedRes = node.getDependencies().getNode(searchableName);
            if (searchedRes != null) {
                return searchedRes;
            }
        }
        return null;
    }
}

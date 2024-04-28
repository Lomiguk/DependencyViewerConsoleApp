package ru.dsckibin.hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Hierarchy extends HashMap<String, Node> {
    private final Set<Node> jar = new HashSet<>();

    public Node addNode(Node newNode) {
        var existedNode = this.get(newNode.getName());
        if (existedNode != null) {
            existedNode.addDependencies(newNode.getDependencies());
            existedNode.setGitView(existedNode.getGitView().equals(GitView.NOT_CHANGED)
                    ? newNode.getGitView()
                    : existedNode.getGitView()
            );
        } else {
            this.put(newNode.getName(), newNode);
        }
        return this.get(newNode.getName());
    }

    public Dependencies registeringDependencyNodes(Dependencies jarClassFieldsAsDependencyNodes) {
        var result = new Dependencies();
        for (var node : jarClassFieldsAsDependencyNodes.keySet()) {
            var dependency = jarClassFieldsAsDependencyNodes.get(node);
            var registeredNode = addNode(node);
            result.put(registeredNode, dependency);
        }
        return result;
    }

    public void addNodeAsJar(Node jarNode) {
        var node = addNode(jarNode);
        jar.add(node);
    }

    public Set<Node> getJarNodes() {
        return jar;
    }
}


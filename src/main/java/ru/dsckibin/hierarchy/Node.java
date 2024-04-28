package ru.dsckibin.hierarchy;

public class Node {
    private final String name;
    private GitView gitView;
    private final Dependencies dependencies = new Dependencies();

    public Node(String name) {
        this.name = name;
        this.gitView = GitView.NOT_CHANGED;
    }

    public String getName() {
        return name;
    }

    public GitView getGitView() {
        return gitView;
    }
    public void setIntervalToGitView() {
        gitView = GitView.INTERVAL;
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    public Node(String name, GitView gitView) {
        this.name = name;
        this.gitView = gitView;
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

    public Node addDependencies(Dependencies newPairs) {
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

    public void setGitView(GitView gitView) {
        this.gitView = gitView;
    }
}

package ru.dsckibin.hierarchy;

public class Node {
    private final String name;
    private GitDiffStatus gitDiffStatus;
    private final Dependencies dependencies = new Dependencies();

    public Node(String name) {
        this.name = name;
        this.gitDiffStatus = GitDiffStatus.NOT_CHANGED;
    }

    public String getName() {
        return name;
    }

    public GitDiffStatus getGitDiffStatus() {
        return gitDiffStatus;
    }
    public void setIntervalToGitView() {
        gitDiffStatus = GitDiffStatus.INTERVAL;
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    public Node(String name, GitDiffStatus gitDiffStatus) {
        this.name = name;
        this.gitDiffStatus = gitDiffStatus;
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

    public void setGitDiffStatus(GitDiffStatus gitDiffStatus) {
        this.gitDiffStatus = gitDiffStatus;
    }
}

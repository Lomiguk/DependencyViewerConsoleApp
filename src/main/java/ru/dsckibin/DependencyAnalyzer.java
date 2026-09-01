package ru.dsckibin;

import ru.dsckibin.hierarchy.HierarchyBuilder;
import ru.dsckibin.hierarchy.Node;
import ru.dsckibin.ui.ConsoleUiManager;
import ru.dsckibin.util.ClassNameUtil;
import ru.dsckibin.util.git.GitMaster;
import ru.dsckibin.util.ignoring.IgnoreUtil;
import ru.dsckibin.util.jar.JarMaster;
import ru.dsckibin.util.visualization.GraphvizDataMapper;
import ru.dsckibin.util.visualization.GraphvizTool;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;

public class DependencyAnalyzer {
    private final GitMaster gitMaster;
    private final String gitRepo;
    private final String jar;
    private final ConsoleUiManager ui = new ConsoleUiManager();
    private final JarMaster jarMaster = new JarMaster();
    private final IgnoreUtil ignoreUtil = new IgnoreUtil();
    private final ClassNameUtil classNameUtil = new ClassNameUtil();
    private final HierarchyBuilder hierarchyBuilder = new HierarchyBuilder(jarMaster, new ClassNameUtil());

    private final GraphvizTool graphvizTool = new GraphvizTool(
            "graph",
            new GraphvizDataMapper(classNameUtil)
    );

    public DependencyAnalyzer() {
        gitRepo = ui.getGitRepo();
        jar = getJarFile(gitRepo);
        gitMaster = new GitMaster(gitRepo);
    }

    public DependencyAnalyzer(String gitRepoPath) {
        gitRepo = getGitRepo(gitRepoPath);
        jar = getJarFile(gitRepo);
        gitMaster = new GitMaster(gitRepo);
    }

    public DependencyAnalyzer(String gitRepoPath, String jarPath) {
        gitRepo = getGitRepo(gitRepoPath);
        jar = validateArchive(jarPath);
        gitMaster = new GitMaster(gitRepo);
    }

    private String getGitRepo(String path) {
        var result = path == null ? ui.getGitRepo() : path;
        if (result == null || result.isBlank() || !Files.isDirectory(Path.of(result))) {
            throw new IllegalArgumentException("repository directory does not exist: " + result);
        }
        return result;
    }

    private String getJarFile(String directory) {
        var jars = jarMaster.searchJar(directory);
        if (jars.isEmpty()) {
            throw new IllegalArgumentException("no JAR, WAR, or ZIP archive found in " + directory);
        }
        if (jars.size() > 1) {
            return ui.select(jars, "archive");
        }
        return jars.get(0);
    }

    private String validateArchive(String path) {
        if (path == null || !Files.isRegularFile(Path.of(path))) {
            throw new IllegalArgumentException("archive does not exist: " + path);
        }
        return path;
    }

    public void start(
            boolean useGitDiff,
            String ignoreFile,
            boolean simplifyNames
    ) {
        List<String> ignoredClasses = ignoreFile == null
                ? List.of()
                : ignoreUtil.getIgnoredNamesFrom(ignoreFile);

        Set<Node> jarNodes;
        if (useGitDiff) {
            var branch = ui.select(gitMaster.getBranches());
            var diffClasses = getChangedClasses(branch);
            jarNodes = hierarchyBuilder.build(
                    jar,
                    diffClasses
            );
        } else {
            jarNodes = hierarchyBuilder.build(jar);
        }

        jarNodesToConsole(jarNodes);

        graphvizTool.drawGraph(
                jarNodes,
                useGitDiff,
                ignoredClasses,
                simplifyNames
        );
    }

    private List<String> getChangedClasses(String branch) {
        var commits = gitMaster.getCommits(branch);
        return gitMaster.getDiff(
                branch,
                ui.select(commits, "first commit").getHash(),
                ui.select(commits, "second commit").getHash()
        );
    }

    private void jarNodesToConsole(Collection<Node> jarNodes) {
        jarNodes.forEach(it -> {
                    System.out.printf("Class: %s; changed in Git diff: %s%n", it.getName(), it.getChangedStatus());
                    it.getDependencies().forEach((key, value) -> {
                                System.out.printf(
                                        "   dep (%s) types: \n",
                                        key
                                );
                                value.forEach((type, weight) -> {
                                    System.out.printf("    * %s - %d \n", type, weight);
                                });
                            }
                    );
                }
        );
    }
}

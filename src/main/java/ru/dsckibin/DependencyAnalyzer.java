package ru.dsckibin;

import ru.dsckibin.hierarchy.HierarchyBuilder;
import ru.dsckibin.hierarchy.Node;
import ru.dsckibin.ui.ConsoleUiManager;
import ru.dsckibin.util.ClassNameUtil;
import ru.dsckibin.util.git.GitMaster;
import ru.dsckibin.util.ignoring.IgnoreUtil;
import ru.dsckibin.util.jar.JarMaster;
import ru.dsckibin.util.vizualization.GraphvizDataMapper;
import ru.dsckibin.util.vizualization.GraphvizTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
            new StringBuilder(),
            new GraphvizDataMapper(classNameUtil),
            classNameUtil
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
        jar = jarPath;
        gitMaster = new GitMaster(gitRepo);

    }

    private String getGitRepo(String path) {
        return path == null ? ui.getGitRepo() : path;
    }

    private String getJarFile(String directory) {
        var jars = jarMaster.searchJar(directory);
        if (jars.size() > 1) {
            return ui.select(jarMaster.searchJar(directory), "jar files");
        } else {
            return jars.get(0);
        }
    }

    public void start(
            Boolean useGitDiff,
            Boolean useIgnoreFile,
            Boolean simplifyNames
    ) {
        List<String> ignoredClasses = getIgnoredNames(useIgnoreFile);

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

    private List<String> getIgnoredNames(Boolean useIgnoreFile) {
        if (useIgnoreFile) {
            return ignoreUtil.getIgnoredNamesFrom("dependency_class.ignore");
        } else {
            return new ArrayList<>();
        }
    }

    private List<String> getChangedClasses(String branch) {
        return gitMaster.getDiff(
                branch,
                ui.select(gitMaster.getCommits(branch)).getHash(),
                ui.select(gitMaster.getCommits(branch)).getHash()
        );
    }

    private void jarNodesToConsole(Collection<Node> jarNodes) {
        jarNodes.forEach(it -> {
                    System.out.printf("Class: %s; In git dif - %s%n", it.getName(), it.getChangedStatus());
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

package ru.dsckibin;

import ru.dsckibin.hierarchy.HierarchyBuilder;
import ru.dsckibin.hierarchy.Node;
import ru.dsckibin.ui.ConsoleUiManager;
import ru.dsckibin.util.FileNameUtil;
import ru.dsckibin.util.asm.ClassNameUtil;
import ru.dsckibin.util.git.GitMaster;
import ru.dsckibin.util.jar.JarMaster;
import ru.dsckibin.util.vizualization.GraphvizDataMapper;
import ru.dsckibin.util.vizualization.GraphvizTool;

import java.util.Collection;

public class DependencyAnalyzer {
    private final GitMaster gitMaster;
    private final String gitRepo;
    private final String jar;
    private final ConsoleUiManager ui = new ConsoleUiManager();
    private final JarMaster jarMaster = new JarMaster();
    private final HierarchyBuilder hierarchyBuilder = new HierarchyBuilder(
            jarMaster,
            new ClassNameUtil(),
            new FileNameUtil()
            );

    private final GraphvizTool graphvizTool = new GraphvizTool(
            "graph",
            new StringBuilder(),
            new GraphvizDataMapper()
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
        return ui.select(jarMaster.searchJar(directory), "jar files");
    }

    public void start() {
        var branch = ui.select(gitMaster.getBranches());

        var diffClasses = gitMaster.getDiff(
                branch,
                ui.select(gitMaster.getCommits(branch)).getHash(),
                ui.select(gitMaster.getCommits(branch)).getHash()
        );

        var jarNodes = hierarchyBuilder.build(jar, diffClasses);

        jarNodesToConsole(jarNodes);

        graphvizTool.drawGraph(jarNodes);
    }

    private void jarNodesToConsole(Collection<Node> jarNodes) {
        jarNodes.forEach(it -> {
                    System.out.printf("Class: %s; In git dif - %s%n", it.getName(), it.getChanged());
                    it.getDependencies().forEach((key, value) -> System.out.printf(
                            "   dep (%s)[%d]: %s\n",
                            key.getTypeOfDependency(),
                            value,
                            key.getName()
                    ));
                }
        );
    }
}

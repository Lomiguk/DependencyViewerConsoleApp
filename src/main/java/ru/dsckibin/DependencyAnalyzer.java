package ru.dsckibin;

import ru.dsckibin.exception.JarFileNotFoundException;
import ru.dsckibin.hierarchy.Hierarchy;
import ru.dsckibin.hierarchy.HierarchyBuilder;
import ru.dsckibin.ui.ConsoleUiManager;
import ru.dsckibin.util.ClassNameUtil;
import ru.dsckibin.util.git.GitMaster;
import ru.dsckibin.util.ignoring.IgnoreUtil;
import ru.dsckibin.util.jar.JarMaster;
import ru.dsckibin.util.vizualization.GraphvizDataMapper;
import ru.dsckibin.util.vizualization.GraphvizTool;

import java.util.ArrayList;
import java.util.List;

public class DependencyAnalyzer {
    private final GitMaster gitMaster;
    private final String gitRepo;
    private final String jar;
    private final ConsoleUiManager ui = new ConsoleUiManager();
    private final JarMaster jarMaster = new JarMaster();
    private final IgnoreUtil ignoreUtil = new IgnoreUtil();
    private final HierarchyBuilder hierarchyBuilder = new HierarchyBuilder(jarMaster, new ClassNameUtil());

    private final GraphvizTool graphvizTool = new GraphvizTool(
            "graph",
            new StringBuilder(),
            new GraphvizDataMapper(new ClassNameUtil())
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
        var result = ui.select(jarMaster.searchJar(directory), "jar files");
        if (result == null) {
            throw new JarFileNotFoundException();
        }
        return result;
    }

    public void start(
            Boolean useGitDiff,
            Boolean useIgnoreFile,
            Boolean simplifyNames
    ) {
        List<String> ignoredClasses = getIgnoredNames(useIgnoreFile);

        Hierarchy hierarchy;
        if (useGitDiff) {
            var branch = ui.select(gitMaster.getBranches());
            var diffClasses = getChangedClasses(branch);
            hierarchy = hierarchyBuilder.buildWithDiff(
                    jar,
                    diffClasses
            );
        } else {
            hierarchy = hierarchyBuilder.buildWithoutDiff(jar);
        }

        ui.printHierarchy(hierarchy.getJarNodes());

        graphvizTool.drawGraph(
                hierarchy.getJarNodes(),
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
}

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DependencyAnalyzer {

    private final static String DEFAULT_DEP_IGNORE = "dependency_class.ignore";

    private final GitMaster gitMaster;
    private final String jar;
    private final ConsoleUiManager ui = new ConsoleUiManager();
    private final JarMaster jarMaster = new JarMaster();
    private final IgnoreUtil ignoreUtil = new IgnoreUtil();
    private final HierarchyBuilder hierarchyBuilder = new HierarchyBuilder(jarMaster, new ClassNameUtil());

    private final String ignorePath;

    private final GraphvizTool graphvizTool = new GraphvizTool(
            "graph",
            new StringBuilder(),
            new GraphvizDataMapper(new ClassNameUtil())
    );

    public DependencyAnalyzer(
            String gitRepoPath,
            String jarPath,
            String pathToIgnore
    ) {
        if (gitRepoPath != null) {
            gitMaster = new GitMaster(gitRepoPath);
        } else {
            gitMaster = null;
        }
        jar = jarPath == null
                ? getJarFilePath(
                        gitRepoPath == null
                                ? ui.getJarParentDir()
                                : gitRepoPath)
                : jarPath;
        ignorePath = pathToIgnore;
    }

    private String getJarFilePath(String directory) {
        var result = ui.select(jarMaster.searchJar(directory), "jar files");
        if (result == null) {
            throw new JarFileNotFoundException();
        }
        return result;
    }

    public void start(
            Boolean simplifyNames
    ) {
        var ignoredClasses = getIgnoredNames(ignorePath);

        var hierarchy = getHierarchy();

        ui.printHierarchy(hierarchy.getJarNodes());

        graphvizTool.drawGraph(
                hierarchy.getJarNodes(),
                gitMaster != null,
                ignoredClasses,
                simplifyNames
        );
    }

    private Hierarchy getHierarchy() {
        if (gitMaster != null) {
            var branch = ui.select(gitMaster.getBranches());
            var diffClasses = getChangedClasses(branch);
            return hierarchyBuilder.buildWithDiff(
                    jar,
                    diffClasses
            );
        } else {
            return hierarchyBuilder.buildWithoutDiff(jar);
        }
    }

    private List<String> getIgnoredNames(String path) {
        if (path != null && new File(path).exists()) {
            return ignoreUtil.getIgnoredNamesFrom(path);
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

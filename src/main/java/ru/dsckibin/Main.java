package ru.dsckibin;

public class Main {
    private final static boolean USE_GIT_DIFF_DEFAULT = true;
    private final static boolean USE_IGNORE_FILE_DEFAULT = true;
    private final static boolean SIMPLIFY_CLASS_NAMES = false;
    public static void main(String[] args) {
        DependencyAnalyzer dependencyAnalyzer;
        if (args.length == 0) {
            dependencyAnalyzer = new DependencyAnalyzer();
        } else if (args.length == 1) {
            dependencyAnalyzer = new DependencyAnalyzer(args[0]);
        } else {
            dependencyAnalyzer = new DependencyAnalyzer(args[0], args[1]);
        }

        dependencyAnalyzer.start(
                USE_GIT_DIFF_DEFAULT,
                USE_IGNORE_FILE_DEFAULT,
                SIMPLIFY_CLASS_NAMES
        );
    }
}

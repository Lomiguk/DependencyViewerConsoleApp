package ru.dsckibin;

public class Main {
    private final static boolean USE_SIMPLIFY_CLASS_NAMES_DEFAULT = true;

    public static void main(String[] args) {
        String projectPath = null;
        String jarPath = null;
        Boolean useSimplifyClassNames = null;
        String pathToIgnoreFile = null;

        for (var i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-p":
                    projectPath = args[i + 1];
                    break;
                case "-j":
                    jarPath = args[i + 1];
                    break;
                case "-i":
                    pathToIgnoreFile = args[i + 1];
                    break;
                case "-s":
                    useSimplifyClassNames = Boolean.valueOf(args[i + 1]);
                    break;
            }
        }

        new DependencyAnalyzer(
                projectPath,
                jarPath,
                pathToIgnoreFile
        ).start(
                useSimplifyClassNames == null ? USE_SIMPLIFY_CLASS_NAMES_DEFAULT : useSimplifyClassNames
        );
    }
}

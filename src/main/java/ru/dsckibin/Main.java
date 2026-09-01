package ru.dsckibin;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String USAGE = """
            Dependency Viewer

            Usage:
              java -jar dependency-viewer.jar [options] <repository> [archive]

            Options:
              --git-diff          analyze only classes changed between two selected commits
              --ignore <file>     exclude class names listed in a file
              --simplify-names    shorten package names in the graph
              --help              show this help

            If repository is omitted, the program asks for it interactively.
            If archive is omitted, the program searches the repository for JAR, WAR, or ZIP files.
            """;

    public static void main(String[] args) {
        try {
            var options = CliOptions.parse(args);
            if (options.help()) {
                System.out.println(USAGE);
                return;
            }

            DependencyAnalyzer analyzer;
            if (options.repository() == null) {
                analyzer = new DependencyAnalyzer();
            } else if (options.archive() == null) {
                analyzer = new DependencyAnalyzer(options.repository());
            } else {
                analyzer = new DependencyAnalyzer(options.repository(), options.archive());
            }

            analyzer.start(options.useGitDiff(), options.ignoreFile(), options.simplifyNames());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Run with --help for usage.");
            System.exit(2);
        }
    }

    record CliOptions(String repository, String archive, boolean useGitDiff,
                      String ignoreFile, boolean simplifyNames, boolean help) {
        static CliOptions parse(String[] args) {
            var positional = new ArrayList<String>();
            String ignoreFile = null;
            boolean useGitDiff = false;
            boolean simplifyNames = false;
            boolean help = false;

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--git-diff" -> useGitDiff = true;
                    case "--simplify-names" -> simplifyNames = true;
                    case "--help", "-h" -> help = true;
                    case "--ignore" -> {
                        if (++i >= args.length) {
                            throw new IllegalArgumentException("--ignore requires a file path");
                        }
                        ignoreFile = args[i];
                    }
                    default -> {
                        if (args[i].startsWith("-")) {
                            throw new IllegalArgumentException("unknown option: " + args[i]);
                        }
                        positional.add(args[i]);
                    }
                }
            }

            if (positional.size() > 2) {
                throw new IllegalArgumentException("expected a repository and optionally an archive");
            }

            return new CliOptions(get(positional, 0), get(positional, 1), useGitDiff,
                    ignoreFile, simplifyNames, help);
        }

        private static String get(List<String> values, int index) {
            return values.size() > index ? values.get(index) : null;
        }
    }
}

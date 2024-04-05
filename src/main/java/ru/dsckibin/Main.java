package ru.dsckibin;

public class Main {
    public static void main(String[] args) {
        DependencyAnalyzer dependencyAnalyzer;
        if (args.length == 0) {
            dependencyAnalyzer = new DependencyAnalyzer();
        } else if (args.length == 1) {
            dependencyAnalyzer = new DependencyAnalyzer(args[0]);
        } else {
            dependencyAnalyzer = new DependencyAnalyzer(args[0], args[1]);
        }

        dependencyAnalyzer.start();
    }
}

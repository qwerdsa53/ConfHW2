package example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.out.println("Usage: java DependencyGraphVisualizer <path-to-plantuml> <path-to-repo>");
            System.exit(1);
        }

        String plantUmlPath = args[0];
        String repoPath = args[1];

        DependencyGraphVisualizer visualizer = new DependencyGraphVisualizer(plantUmlPath, repoPath);
        visualizer.generateDependencyGraph();
    }
}

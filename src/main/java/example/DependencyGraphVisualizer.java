package example;


import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DependencyGraphVisualizer {

    private final String plantUmlPath;
    private final String repoPath;
    private final ProcessExecutor processExecutor;

    public DependencyGraphVisualizer(String plantUmlPath, String repoPath) {
        this.plantUmlPath = plantUmlPath;
        this.repoPath = repoPath;
        this.processExecutor = new ProcessExecutorImpl();
    }

    public DependencyGraphVisualizer(String repoPath, String plantUmlPath, ProcessExecutor processExecutor) {
        this.repoPath = repoPath;
        this.plantUmlPath = plantUmlPath;
        this.processExecutor = processExecutor;
    }

    public DependencyGraphVisualizer() {
        this.plantUmlPath = "defaultPath";
        this.repoPath = "defaultRepoPath";
        this.processExecutor = new ProcessExecutorImpl();
    }


    public void generateDependencyGraph() throws IOException, InterruptedException {
        List<CommitNode> commits = collectCommits();
        String plantUmlSource = generatePlantUmlSource(commits);
        Path plantUmlFile = Files.createTempFile("dependency_graph", ".puml");
        Files.write(plantUmlFile, plantUmlSource.getBytes());
        Process process = processExecutor.startProcess("java", "-jar", plantUmlPath, plantUmlFile.toString());
        process.getOutputStream().close();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            System.out.println("Ошибка при запуске PlantUML. Код завершения: " + exitCode);
        } else {
            System.out.println("Граф зависимостей успешно сгенерирован.");
            System.out.println("Файл PlantUML сохранён здесь: " + plantUmlFile.toAbsolutePath());
            System.out.println("Ожидаемый графический файл: " + plantUmlFile.toAbsolutePath().toString().replace(".puml", ".png"));
        }
    }


    public @NotNull List<CommitNode> collectCommits() throws IOException, InterruptedException {
        List<CommitNode> commitNodes = new ArrayList<>();
        Process process = processExecutor.startProcess("git", "-C", repoPath, "log", "--all", "--pretty=format:%H|%s|%P");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                String commitHash = parts[0];
                String commitMessage = parts[1];
                List<String> parentHashes = parts.length > 2 ? Arrays.asList(parts[2].split(" ")) : new ArrayList<>();

                CommitNode node = analyzeCommit(commitHash, commitMessage, parentHashes);
                commitNodes.add(node);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Ошибка при выполнении команды git log. Код возврата: " + exitCode);
        }

        return commitNodes;
    }


    public @NotNull CommitNode analyzeCommit(String commitHash, String commitMessage, List<String> parentHashes) throws IOException, InterruptedException {
        List<String> modifiedFiles = new ArrayList<>();
        Process process = processExecutor.startProcess("git", "-C", repoPath, "diff-tree", "--no-commit-id", "--name-only", "-r", commitHash);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String filePath;
            while ((filePath = reader.readLine()) != null) {
                modifiedFiles.add(filePath);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Ошибка при выполнении команды git diff-tree для коммита " + commitHash + ". Код возврата: " + exitCode);
        }

        return new CommitNode(commitHash, commitMessage, modifiedFiles, parentHashes);
    }

    public @NotNull String generatePlantUmlSource(@NotNull List<CommitNode> commits) {
        StringBuilder plantUmlSource = new StringBuilder();
        plantUmlSource.append("@startuml\n");

        for (CommitNode node : commits) {
            plantUmlSource.append("class ").append(node.getCommitHash()).append(" {\n");
            plantUmlSource.append("  \"").append(node.getCommitMessage()).append("\"\n");
            for (String file : node.getModifiedFiles()) {
                plantUmlSource.append("  ").append(file).append("\n");
            }
            plantUmlSource.append("}\n");
        }

        for (CommitNode node : commits) {
            for (String parentHash : node.getParentHashes()) {
                if (!parentHash.isEmpty()) {
                    plantUmlSource.append(parentHash)
                            .append(" --> ")
                            .append(node.getCommitHash())
                            .append("\n");
                }
            }
        }

        plantUmlSource.append("@enduml\n");
        return plantUmlSource.toString();
    }

}
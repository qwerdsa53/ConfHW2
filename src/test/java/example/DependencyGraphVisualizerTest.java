package example;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DependencyGraphVisualizerTest {

    @Test
    void testCollectCommits() throws IOException, InterruptedException {
        ProcessExecutor mockExecutor = mock(ProcessExecutor.class);
        Process mockProcessLog = mock(Process.class);
        Process mockProcessDiffTree1 = mock(Process.class);
        Process mockProcessDiffTree2 = mock(Process.class);

        when(mockExecutor.startProcess("git", "-C", "path/to/repo", "log", "--all", "--pretty=format:%H|%s|%P"))
                .thenReturn(mockProcessLog);
        when(mockProcessLog.getInputStream())
                .thenReturn(new ByteArrayInputStream((
                        "abc123|Initial commit|def456\n" +
                                "def456|Second commit\n"
                ).getBytes()));

        when(mockExecutor.startProcess("git", "-C", "path/to/repo", "diff-tree", "--no-commit-id", "--name-only", "-r", "abc123"))
                .thenReturn(mockProcessDiffTree1);
        when(mockProcessDiffTree1.getInputStream())
                .thenReturn(new ByteArrayInputStream("file1.txt\nfile2.txt\n".getBytes()));

        when(mockExecutor.startProcess("git", "-C", "path/to/repo", "diff-tree", "--no-commit-id", "--name-only", "-r", "def456"))
                .thenReturn(mockProcessDiffTree2);
        when(mockProcessDiffTree2.getInputStream())
                .thenReturn(new ByteArrayInputStream("file2.txt\nfile3.txt\n".getBytes()));

        DependencyGraphVisualizer visualizer = new DependencyGraphVisualizer("path/to/repo", "path/to/plantuml", mockExecutor);

        List<CommitNode> commits = visualizer.collectCommits();

        assertEquals(2, commits.size());

        assertEquals("abc123", commits.get(0).getCommitHash());
        assertEquals("Initial commit", commits.get(0).getCommitMessage());
        assertEquals(List.of("file1.txt", "file2.txt"), commits.get(0).getModifiedFiles());
        assertEquals("def456", commits.get(0).getParentHashes().get(0));

        assertEquals("def456", commits.get(1).getCommitHash());
        assertEquals("Second commit", commits.get(1).getCommitMessage());
        assertEquals(List.of("file2.txt", "file3.txt"), commits.get(1).getModifiedFiles());
        assertEquals(0, commits.get(1).getParentHashes().size());
    }

    @Test
    void testAnalyzeCommit() throws IOException, InterruptedException {
        ProcessExecutor mockExecutor = mock(ProcessExecutor.class);
        Process mockProcess = mock(Process.class);

        when(mockExecutor.startProcess("git", "-C", "path/to/repo", "diff-tree", "--no-commit-id", "--name-only", "-r", "abc123"))
                .thenReturn(mockProcess);
        when(mockProcess.getInputStream())
                .thenReturn(new ByteArrayInputStream("file1.txt\nfile2.txt\n".getBytes()));

        DependencyGraphVisualizer visualizer = new DependencyGraphVisualizer("path/to/repo", "path/to/plantuml", mockExecutor);

        CommitNode commitNode = visualizer.analyzeCommit("abc123", "Sample commit", List.of("def456"));

        assertEquals("abc123", commitNode.getCommitHash());
        assertEquals("Sample commit", commitNode.getCommitMessage());
        assertEquals(List.of("file1.txt", "file2.txt"), commitNode.getModifiedFiles());
        assertEquals(List.of("def456"), commitNode.getParentHashes());
    }

    @Test
    void testGeneratePlantUmlSource() {
        List<CommitNode> commits = List.of(
                new CommitNode("abc123", "Initial commit", List.of("file1.txt"), List.of()),
                new CommitNode("def456", "Second commit", List.of("file2.txt"), List.of("abc123"))
        );

        DependencyGraphVisualizer visualizer = new DependencyGraphVisualizer();
        String umlSource = visualizer.generatePlantUmlSource(commits);

        assertTrue(umlSource.contains("class abc123"));
        assertTrue(umlSource.contains("class def456"));
        assertTrue(umlSource.contains("abc123 --> def456"));
    }
}
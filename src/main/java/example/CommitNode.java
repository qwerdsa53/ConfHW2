package example;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommitNode {
    private final String commitHash;
    private final String commitMessage;
    private final List<String> modifiedFiles;
    private final List<String> parentHashes;
}


package example;

import java.io.IOException;

public interface ProcessExecutor {
    Process startProcess(String... command) throws IOException;
}

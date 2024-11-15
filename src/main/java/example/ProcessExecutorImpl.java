package example;

import java.io.IOException;

public class ProcessExecutorImpl implements ProcessExecutor {
    @Override
    public Process startProcess(String... command) throws IOException {
        return new ProcessBuilder(command).start();
    }
}

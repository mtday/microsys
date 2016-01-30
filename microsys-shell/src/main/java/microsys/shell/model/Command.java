package microsys.shell.model;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public abstract class Command {
    private final ShellEnvironment shellEnvironment;

    /**
     * @param shellEnvironment the shell command execution environment
     */
    public Command(final ShellEnvironment shellEnvironment) {
        this.shellEnvironment = Objects.requireNonNull(shellEnvironment);
    }

    protected ShellEnvironment getShellEnvironment() {
        return this.shellEnvironment;
    }

    public abstract List<Registration> getRegistrations();

    public abstract CommandStatus process(UserCommand userCommand, PrintWriter writer);
}

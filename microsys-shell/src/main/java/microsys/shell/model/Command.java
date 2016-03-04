package microsys.shell.model;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The base class for shell commands.
 */
public abstract class Command {
    @Nonnull
    private final ShellEnvironment shellEnvironment;

    /**
     * @param shellEnvironment the shell command execution environment
     */
    public Command(@Nonnull final ShellEnvironment shellEnvironment) {
        this.shellEnvironment = Objects.requireNonNull(shellEnvironment);
    }

    /**
     * @return the shell command execution environment
     */
    @Nonnull
    protected ShellEnvironment getShellEnvironment() {
        return this.shellEnvironment;
    }

    /**
     * @return the {@link Registration} objects describing the shell commands provided by this command
     */
    @Nonnull
    public abstract List<Registration> getRegistrations();

    /**
     * @param userCommand the command entered by the user that needs to be executed
     * @param writer the writer to which the command output should be written
     * @return the {@link CommandStatus} describing the result of the command execution
     */
    @Nonnull
    public abstract CommandStatus process(@Nonnull final UserCommand userCommand, @Nonnull final PrintWriter writer);
}

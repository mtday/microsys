package microsys.shell.command.service;

import microsys.shell.completer.ServiceHostCompleter;
import microsys.shell.completer.ServicePortCompleter;
import microsys.shell.completer.ServiceTypeCompleter;
import microsys.shell.completer.ServiceVersionCompleter;
import microsys.shell.model.Command;
import microsys.shell.model.Option;
import microsys.shell.model.ShellEnvironment;

import java.util.Optional;

/**
 * This command provides some of the common functionality between the service commands.
 */
public abstract class BaseServiceCommand extends Command {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public BaseServiceCommand(final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input the service type
     */
    protected Option getTypeOption(final String description) {
        return new Option(description, "t", Optional.of("type"), Optional.of("type"),
                1, false, false, Optional.of(new ServiceTypeCompleter()));
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input the service host
     */
    protected Option getHostOption(final String description) {
        return new Option(description, "h", Optional.of("host"), Optional.of("host"), 1, false, false,
                Optional.of(new ServiceHostCompleter(getShellEnvironment())));
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input the service port
     */
    protected Option getPortOption(final String description) {
        return new Option(description, "p", Optional.of("port"), Optional.of("port"), 1, false, false,
                Optional.of(new ServicePortCompleter(getShellEnvironment())));
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input the service version
     */
    protected Option getVersionOption(final String description) {
        return new Option(description, "v", Optional.of("version"), Optional.of("version"), 1, false, false,
                Optional.of(new ServiceVersionCompleter(getShellEnvironment())));
    }
}

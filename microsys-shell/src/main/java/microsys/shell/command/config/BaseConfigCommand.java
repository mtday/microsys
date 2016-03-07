package microsys.shell.command.config;

import microsys.shell.completer.ConfigTypeCompleter;
import microsys.shell.model.Command;
import microsys.shell.model.Option;
import microsys.shell.model.ShellEnvironment;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * This command provides some of the common functionality between the config commands.
 */
public abstract class BaseConfigCommand extends Command {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public BaseConfigCommand(@Nonnull final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input the system configuration type
     */
    @Nonnull
    protected Option getTypeOption(@Nonnull final String description) {
        return new Option(description, "t", Optional.of("type"), Optional.of("type"), 1, false, false,
                Optional.of(new ConfigTypeCompleter()));
    }

    /**
     * @param description the description to include in the option
     * @return the {@link Option} used to input a filter to use when limiting the display of system configuration info
     */
    @Nonnull
    protected Option getFilterOption(@Nonnull final String description) {
        return new Option(
                description, "f", Optional.of("filter"), Optional.of("filter"), 1, false, false, Optional.empty());
    }
}

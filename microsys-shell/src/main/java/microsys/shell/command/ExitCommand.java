package microsys.shell.command;

import microsys.shell.model.Command;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * This command implements the {@code exit} and {@code quit} commands in the shell.
 */
public class ExitCommand extends Command {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public ExitCommand(@Nonnull final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<Registration> getRegistrations() {
        final Optional<String> description = Optional.of("exit the shell");
        final CommandPath exit = new CommandPath("exit");
        final CommandPath quit = new CommandPath("quit");

        final Registration exitReg = new Registration(exit, Optional.empty(), description);
        final Registration quitReg = new Registration(quit, Optional.empty(), description);

        return Arrays.asList(exitReg, quitReg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CommandStatus process(@Nonnull final UserCommand userCommand, @Nonnull final PrintWriter writer) {
        writer.println("Terminating");
        return CommandStatus.TERMINATE;
    }
}

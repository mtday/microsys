package microsys.shell.command;

import com.typesafe.config.Config;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.model.Command;
import microsys.shell.model.CommandPath;
import microsys.shell.model.Registration;
import microsys.shell.model.UserCommand;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This command implements the {@code exit} and {@code quit} commands in the shell.
 */
public class ExitCommand extends Command {
    /**
     * @param config
     * @param discoveryManager
     */
    public ExitCommand(final Config config, final DiscoveryManager discoveryManager) {
        super(config, discoveryManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public boolean process(final UserCommand userCommand) {
        return false;
    }
}

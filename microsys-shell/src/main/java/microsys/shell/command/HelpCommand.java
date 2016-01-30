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
 * This actor implements the {@code help} command in the shell.
 */
public class HelpCommand extends Command {
    /**
     * @param config
     * @param discoveryManager
     */
    public HelpCommand(final Config config, final DiscoveryManager discoveryManager) {
        super(config, discoveryManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> getRegistrations() {
        final Optional<String> description = Optional.of("display usage information for available shell commands");
        final CommandPath help = new CommandPath("help");
        return Arrays.asList(new Registration(help, Optional.empty(), description));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(final UserCommand userCommand) {
        final CommandPath path = userCommand.getCommandPath();
        if (path.getSize() > 1) {
            // Strip off the "help" at the front and lookup the registrations for which help should be retrieved.
            path.getChild().get();
        } else {
            // Request all of the available registrations.
        }
        return false;
    }
}

package microsys.shell.command.service;

import microsys.service.model.Service;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Options;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * This command implements the {@code service memory} command in the shell.
 */
public class MemoryCommand extends BaseServiceCommand {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public MemoryCommand(final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> getRegistrations() {
        final Option type = getTypeOption("the service type to show memory information");
        final Option host = getHostOption("the host of the service to show memory information");
        final Option port = getPortOption("the port of the service to show memory information");
        final Option version = getVersionOption("the version of the service to show memory information");
        final Optional<Options> options = Optional.of(new Options(type, host, port, version));

        final Optional<String> desc = Optional.of("display memory usage information for one or more services");
        final CommandPath commandPath = new CommandPath("service", "memory");
        return Collections.singletonList(new Registration(commandPath, options, desc));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandStatus process(final UserCommand userCommand, final PrintWriter writer) {
        try {
            final SortedSet<Service> services = getShellEnvironment().getDiscoveryManager().getAll();
            final ServiceFilter filter = new ServiceFilter(userCommand.getCommandLine());

            final List<Service> filtered = services.stream().filter(filter::matches).collect(Collectors.toList());

            // TODO
        } catch (final Exception exception) {
            writer.println("Failed to retrieve available services: " + exception.getMessage());
        }

        return CommandStatus.SUCCESS;
    }
}

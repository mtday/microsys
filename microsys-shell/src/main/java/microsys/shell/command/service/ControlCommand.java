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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * This command implements the {@code service control} commands in the shell.
 */
public class ControlCommand extends BaseServiceCommand {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public ControlCommand(final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> getRegistrations() {
        final Option type = getTypeOption("the service type to control");
        final Option host = getHostOption("the host of the service to control");
        final Option port = getPortOption("the port of the service to control");
        final Option version = getVersionOption("the version of the service to control");
        final Optional<Options> options = Optional.of(new Options(type, host, port, version));

        final Optional<String> stopDescription = Optional.of("request the stop of one or more services");
        final CommandPath serviceStopPath = new CommandPath("service", "control", "stop");
        final Registration serviceStop = new Registration(serviceStopPath, options, stopDescription);

        final Optional<String> restartDescription = Optional.of("request the restart of one or more services");
        final CommandPath serviceRestartPath = new CommandPath("service", "control", "restart");
        final Registration serviceRestart = new Registration(serviceRestartPath, options, restartDescription);

        return Arrays.asList(serviceStop, serviceRestart);
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

            if (userCommand.getCommandPath().equals(new CommandPath("service", "control", "stop"))) {
                return handleStop(filtered, writer);
            } else {
                return handleRestart(filtered, writer);
            }
        } catch (final Exception exception) {
            writer.println("Failed to retrieve available services: " + exception.getMessage());
        }

        return CommandStatus.SUCCESS;
    }

    protected CommandStatus handleStop(final List<Service> services, final PrintWriter writer) {
        // TODO
        return CommandStatus.SUCCESS;
    }

    protected CommandStatus handleRestart(final List<Service> services, final PrintWriter writer) {
        // TODO
        return CommandStatus.SUCCESS;
    }
}

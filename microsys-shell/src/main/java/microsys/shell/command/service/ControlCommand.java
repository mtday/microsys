package microsys.shell.command.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import microsys.service.model.Service;
import microsys.service.model.ServiceControlStatus;
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
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

            writer.println(new ServiceSummary(services.size(), filtered.size()));
            if (!filtered.isEmpty()) {
                if (userCommand.getCommandPath().equals(new CommandPath("service", "control", "stop"))) {
                    return handleStop(filtered, writer);
                } else {
                    return handleRestart(filtered, writer);
                }
            }
        } catch (final Exception exception) {
            writer.println("Failed to retrieve available services: " + ExceptionUtils.getMessage(exception));
        }

        return CommandStatus.SUCCESS;
    }

    protected CommandStatus handleStop(final List<Service> services, final PrintWriter writer) throws Exception {
        return control(getShellEnvironment().getServiceClient().stop(services), writer);
    }

    protected CommandStatus handleRestart(final List<Service> services, final PrintWriter writer) throws Exception {
        return control(getShellEnvironment().getServiceClient().restart(services), writer);
    }

    protected CommandStatus control(final Future<Map<Service, ServiceControlStatus>> future, final PrintWriter writer)
            throws Exception {
        final Map<Service, ServiceControlStatus> map = future.get(10, TimeUnit.SECONDS);
        final Stringer stringer = new Stringer(map.keySet());
        map.entrySet().stream().map(stringer::toString).forEach(writer::println);
        return CommandStatus.SUCCESS;
    }

    protected static class Stringer {
        private final OptionalInt longestType;
        private final OptionalInt longestHost;
        private final OptionalInt longestPort;

        public Stringer(final Set<Service> services) {
            this.longestType = services.stream().mapToInt(s -> s.getType().name().length()).max();
            this.longestHost = services.stream().mapToInt(s -> s.getHost().length()).max();
            this.longestPort = services.stream().mapToInt(s -> String.valueOf(s.getPort()).length()).max();
        }

        public String toString(final Map.Entry<Service, ServiceControlStatus> entry) {
            final Service service = entry.getKey();
            final ServiceControlStatus status = entry.getValue();

            final String type = StringUtils.rightPad(service.getType().name(), this.longestType.getAsInt());
            final String host = StringUtils.rightPad(service.getHost(), this.longestHost.getAsInt());
            final String port = StringUtils.rightPad(String.valueOf(service.getPort()), this.longestPort.getAsInt());

            final String action = status.getAction();
            final boolean success = status.isSuccess();

            return String.format("    %s  %s  %s  - %s in progress: %s", type, host, port, action, success);
        }
    }
}

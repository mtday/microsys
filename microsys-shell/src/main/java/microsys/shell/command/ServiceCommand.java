package microsys.shell.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import microsys.common.model.ServiceType;
import microsys.service.model.Service;
import microsys.shell.model.Command;
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
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * This command implements the {@code cluster} commands in the shell.
 */
public class ServiceCommand extends Command {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public ServiceCommand(final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> getRegistrations() {
        final Option listType =
                new Option("the service type to list", "t", Optional.of("type"), Optional.of("type"), 1, false, false);
        final Option listHost =
                new Option("the host to list", "h", Optional.of("host"), Optional.of("host"), 1, false, false);
        final Optional<Options> listOptions = Optional.of(new Options(listType, listHost));

        final Optional<String> listDescription = Optional.of("provides information about the available services");
        final CommandPath serviceListPath = new CommandPath("service", "list");
        final Registration serviceList = new Registration(serviceListPath, listOptions, listDescription);

        final Option restartType =
                new Option("the service type to restart", "t", Optional.of("type"), Optional.of("type"), 1, false,
                        false);
        final Option restartHost =
                new Option("the host of the service to restart", "h", Optional.of("host"), Optional.of("host"), 1,
                        false, false);
        final Option restartPort =
                new Option("the port of the service to restart", "p", Optional.of("port"), Optional.of("port"), 1,
                        false, false);
        final Optional<Options> restartOptions = Optional.of(new Options(restartType, restartHost, restartPort));

        final Optional<String> restartDescription = Optional.of("request the restart of a service");
        final CommandPath serviceRestartPath = new CommandPath("service", "restart");
        final Registration serviceRestart = new Registration(serviceRestartPath, restartOptions, restartDescription);

        return Arrays.asList(serviceList, serviceRestart);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandStatus process(final UserCommand userCommand, final PrintWriter writer) {
        if (userCommand.getCommandPath().equals(new CommandPath("service", "list"))) {
            return handleList(userCommand, writer);
        } else {
            return handleRestart(userCommand, writer);
        }
    }

    protected CommandStatus handleList(final UserCommand userCommand, final PrintWriter writer) {
        final SortedSet<Service> services = getShellEnvironment().getDiscoveryManager().getAll();
        final Filter filter = new Filter(userCommand.getCommandLine());
        final Stringer stringer = new Stringer(services);

        final List<String> output =
                services.stream().filter(filter::matches).map(stringer::toString).collect(Collectors.toList());

        writer.println(new Summary(services.size(), output.size()));
        output.forEach(writer::println);

        return CommandStatus.SUCCESS;
    }

    protected CommandStatus handleRestart(final UserCommand userCommand, final PrintWriter writer) {
        // TODO
        return CommandStatus.SUCCESS;
    }

    protected static class Summary {
        private final int totalServices;
        private final int matchingServices;

        public Summary(final int totalServices, final int matchingServices) {
            this.totalServices = totalServices;
            this.matchingServices = matchingServices;
        }

        @Override
        public String toString() {
            if (totalServices == 0) {
                return "No services are running";
            } else if (matchingServices == 0) {
                return String.format("None of the running services match", totalServices);
            }
            if (totalServices != matchingServices) {
                if (matchingServices == 1) {
                    return String.format("Displaying the matching service (of %d)", matchingServices, totalServices);
                }
                return String.format("Displaying %d matching services (of %d)", matchingServices, totalServices);
            }

            if (totalServices == 1) {
                return String.format("Displaying the single available service");
            }
            return String.format("Displaying all %d services", totalServices);
        }
    }

    protected static class Stringer {
        private final OptionalInt longestType;
        private final OptionalInt longestHost;
        private final OptionalInt longestPort;

        public Stringer(final SortedSet<Service> services) {
            this.longestType = services.stream().mapToInt(s -> s.getType().name().length()).max();
            this.longestHost = services.stream().mapToInt(s -> s.getHost().length()).max();
            this.longestPort = services.stream().mapToInt(s -> String.valueOf(s.getPort()).length()).max();
        }

        public String toString(final Service service) {
            final String type = StringUtils.rightPad(service.getType().name(), longestType.getAsInt());
            final String host = StringUtils.rightPad(service.getHost(), longestHost.getAsInt());
            final String port = StringUtils.rightPad(String.valueOf(service.getPort()), longestPort.getAsInt());
            return String.format("    %s  %s  %s", type, host, port);
        }
    }

    protected static class Filter {
        private final Optional<ServiceType> type;
        private final Optional<String> host;

        public Filter(final Optional<CommandLine> commandLine) {
            if (commandLine.isPresent()) {
                if (commandLine.get().hasOption('t')) {
                    this.type = Optional.of(ServiceType.valueOf(commandLine.get().getOptionValue('t').toUpperCase()));
                } else {
                    this.type = Optional.empty();
                }
                if (commandLine.get().hasOption('h')) {
                    this.host = Optional.of(commandLine.get().getOptionValue('h').toUpperCase());
                } else {
                    this.host = Optional.empty();
                }
            } else {
                this.type = Optional.empty();
                this.host = Optional.empty();
            }
        }

        public boolean matches(final Service service) {
            boolean matches = true;
            if (this.type.isPresent() && !this.type.get().equals(service.getType())) {
                matches = false;
            }
            if (this.host.isPresent() && !StringUtils.equalsIgnoreCase(this.host.get(), service.getHost())) {
                matches = false;
            }
            return matches;
        }
    }
}

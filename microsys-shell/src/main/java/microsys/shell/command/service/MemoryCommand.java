package microsys.shell.command.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import microsys.service.model.Service;
import microsys.service.model.ServiceMemory;
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
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

            writer.println(new ServiceSummary(services.size(), filtered.size()));

            if (!filtered.isEmpty()) {
                final Future<Map<Service, ServiceMemory>> future =
                        getShellEnvironment().getServiceClient().getMemory(filtered);
                final Map<Service, ServiceMemory> memoryMap = future.get(10, TimeUnit.SECONDS);

                final Stringer stringer = new Stringer(filtered);
                memoryMap.entrySet().stream().map(stringer::toString).forEach(writer::println);
            }
        } catch (final Exception exception) {
            writer.println("Failed to retrieve available services: " + ExceptionUtils.getMessage(exception));
        }

        return CommandStatus.SUCCESS;
    }

    protected static class Stringer {
        private final OptionalInt longestType;
        private final OptionalInt longestHost;
        private final OptionalInt longestPort;

        public Stringer(final List<Service> services) {
            this.longestType = services.stream().mapToInt(s -> s.getType().name().length()).max();
            this.longestHost = services.stream().mapToInt(s -> s.getHost().length()).max();
            this.longestPort = services.stream().mapToInt(s -> String.valueOf(s.getPort()).length()).max();
        }

        public String toString(final Map.Entry<Service, ServiceMemory> entry) {
            final Service service = entry.getKey();
            final ServiceMemory memory = entry.getValue();

            final String type = StringUtils.rightPad(service.getType().name(), this.longestType.getAsInt());
            final String host = StringUtils.rightPad(service.getHost(), this.longestHost.getAsInt());
            final String port = StringUtils.rightPad(String.valueOf(service.getPort()), this.longestPort.getAsInt());

            final String heapUsed = readable(memory.getHeapUsed());
            final String heapAvailable = readable(memory.getHeapAvailable());
            final double heapPct = memory.getHeapUsedPercent();

            final String nonheapUsed = readable(memory.getNonHeapUsed());
            final String nonheapAvailable = readable(memory.getNonHeapAvailable());
            final double nonheapPct = memory.getNonHeapUsedPercent() < 0 ? 0 : memory.getNonHeapUsedPercent();

            return String.format("    %s  %s  %s  Heap: %s of %s (%.2f%%), Non-Heap: %s of %s (%.2f%%)",
                    type, host, port, heapUsed, heapAvailable, heapPct, nonheapUsed, nonheapAvailable, nonheapPct);
        }

        public String readable(final long bytes) {
            if (bytes < 0) {
                return "unknown";
            } else if (bytes < 1024) {
                // Bytes
                return String.format("%db", bytes);
            } else if (bytes < 1024 * 1024) {
                // Kilobytes
                return String.format("%.02fk", (double) bytes / 1024);
            } else if (bytes < 1024 * 1024 * 1024) {
                // Megabytes
                return String.format("%.02fM", (double) bytes / 1024 / 1024);
            } else {
                // Gigabytes
                return String.format("%.02fG", (double) bytes / 1024 / 1024 / 1024);
            }
        }
    }
}

package microsys.shell.command.service;

import microsys.common.model.ServiceType;
import microsys.service.model.Service;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * Responsible for performing filtering operations on {@link Service} objects based on user-provided command-line
 * parameters from the shell.
 */
public class ServiceFilter {
    private final Optional<ServiceType> type;
    private final Optional<String> host;
    private final Optional<Integer> port;
    private final Optional<String> version;

    public ServiceFilter(final Optional<CommandLine> commandLine) {
        if (commandLine.isPresent()) {
            if (commandLine.get().hasOption('t')) {
                this.type = Optional.of(ServiceType.valueOf(commandLine.get().getOptionValue('t').toUpperCase()));
            } else {
                this.type = Optional.empty();
            }
            if (commandLine.get().hasOption('h')) {
                this.host = Optional.of(commandLine.get().getOptionValue('h'));
            } else {
                this.host = Optional.empty();
            }
            if (commandLine.get().hasOption('p') && StringUtils.isNumeric(commandLine.get().getOptionValue('p'))) {
                this.port = Optional.of(Integer.parseInt(commandLine.get().getOptionValue('p'), 10));
            } else {
                this.port = Optional.empty();
            }
            if (commandLine.get().hasOption('v')) {
                this.version = Optional.of(commandLine.get().getOptionValue('v'));
            } else {
                this.version = Optional.empty();
            }
        } else {
            this.type = Optional.empty();
            this.host = Optional.empty();
            this.port = Optional.empty();
            this.version = Optional.empty();
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
        if (this.port.isPresent() && this.port.get() != service.getPort()) {
            matches = false;
        }
        if (this.version.isPresent() && !StringUtils.equalsIgnoreCase(this.version.get(), service.getVersion())) {
            matches = false;
        }
        return matches;
    }
}

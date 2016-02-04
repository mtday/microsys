package microsys.shell.completer;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import microsys.service.model.Service;
import microsys.shell.model.ShellEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Responsible for performing tab-completions on service host values, using service discovery to determine what valid
 * hosts are available.
 */
public class ServiceHostCompleter implements Completer {
    private final ShellEnvironment shellEnvironment;

    /**
     * @param shellEnvironment the {@link ShellEnvironment} containing all of the necessary information for
     * performing the tab completion
     */
    public ServiceHostCompleter(final ShellEnvironment shellEnvironment) {
        this.shellEnvironment = Objects.requireNonNull(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        final List<String> hosts = this.shellEnvironment.getDiscoveryManager().getAll().stream().map(Service::getHost)
                .collect(Collectors.toList());
        return new StringsCompleter(hosts).complete(buffer, cursor, candidates);
    }
}

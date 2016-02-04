package microsys.shell.completer;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import microsys.service.model.Service;
import microsys.shell.model.ShellEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Responsible for performing tab-completions on service ports by using service discovery to find valid ports.
 */
public class ServicePortCompleter implements Completer {
    private final ShellEnvironment shellEnvironment;

    /**
     * @param shellEnvironment the {@link ShellEnvironment} that contains all of the necessary information for
     * performing the tab completion
     */
    public ServicePortCompleter(final ShellEnvironment shellEnvironment) {
        this.shellEnvironment = Objects.requireNonNull(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        final List<String> ports =
                this.shellEnvironment.getDiscoveryManager().getAll().stream().map(Service::getPort).map(String::valueOf)
                        .collect(Collectors.toList());
        return new StringsCompleter(ports).complete(buffer, cursor, candidates);
    }
}

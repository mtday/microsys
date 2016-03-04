package microsys.shell.completer;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import microsys.service.model.Service;
import microsys.shell.model.ShellEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Responsible for performing tab-completions on service version values, using service discovery to determine what valid
 * versions are available.
 */
public class ServiceVersionCompleter implements Completer {
    @Nonnull
    private final ShellEnvironment shellEnvironment;

    /**
     * @param shellEnvironment the {@link ShellEnvironment} containing all of the necessary information for
     * performing the tab completion
     */
    public ServiceVersionCompleter(@Nonnull final ShellEnvironment shellEnvironment) {
        this.shellEnvironment = Objects.requireNonNull(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int complete(@Nonnull final String buffer, final int cursor, @Nonnull final List<CharSequence> candidates) {
        try {
            final List<String> versions =
                    this.shellEnvironment.getDiscoveryManager().getAll().stream().map(Service::getVersion)
                            .collect(Collectors.toList());
            return new StringsCompleter(versions).complete(buffer, cursor, candidates);
        } catch (final Exception exception) {
            return -1;
        }
    }
}

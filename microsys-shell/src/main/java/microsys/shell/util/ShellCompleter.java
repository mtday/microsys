package microsys.shell.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import microsys.shell.RegistrationManager;
import microsys.shell.completer.CompletionTree;
import microsys.shell.model.Registration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Responsible for managing tab-completion within the shell.
 */
public class ShellCompleter implements Completer {
    private final static Logger LOG = LoggerFactory.getLogger(ShellCompleter.class);

    private final CompletionTree completions;

    /**
     * @param registrationManager the {@link RegistrationManager} that is tracking the supported shell commands
     */
    public ShellCompleter(final RegistrationManager registrationManager) {
        this(Objects.requireNonNull(registrationManager).getRegistrations());
    }

    /**
     * @param registrations the {@link Registration} objects that define the available completions
     */
    public ShellCompleter(final Collection<Registration> registrations) {
        Objects.requireNonNull(registrations);

        this.completions = new CompletionTree();
        registrations.stream().map(Registration::getCompletions).forEach(this.completions::merge);
    }

    /**
     * @param registrations the {@link Registration} objects that define the available completions
     */
    public ShellCompleter(final Registration... registrations) {
        this(Arrays.asList(Objects.requireNonNull(registrations)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        final ArgumentCompleter.ArgumentList argumentList =
                new ArgumentCompleter.WhitespaceArgumentDelimiter().delimit(buffer, cursor);

        CompletionTree current = this.completions;
        if (argumentList.getArguments().length == 0) {
            candidates.addAll(current.getChildrenCandidates());
            return cursor;
        } else {
            final List<String> args = Arrays.asList(argumentList.getArguments());
            for (int i = 0; i <= argumentList.getCursorArgumentIndex(); i++) {
                final boolean isCursorArg = (i == argumentList.getCursorArgumentIndex());
                final String arg = i >= args.size() ? "" : args.get(i);

                if (isCursorArg) {
                    final List<CompletionTree> matches = current.getChildrenMatching(arg);

                    if (matches.size() == 1 && matches.get(0).getCompleter().isPresent()) {
                        // Let the matching completer do the work.
                        final Completer completer = matches.get(0).getCompleter().get();
                        return cursor + completer.complete("", 0, candidates);
                    } else {
                        matches.stream().map(CompletionTree::getCandidate).filter(Optional::isPresent).map(Optional
                                ::get).sorted().forEach(candidates::add);
                        return cursor - arg.length();
                    }
                } else {
                    final Optional<CompletionTree> match = current.getChild(arg);
                    if (match.isPresent()) {
                        // User has already entered this tree node. Continue to the next argument.
                        current = match.get();
                    } else {
                        // No idea what the user has typed, it is not recognized.
                        return cursor;
                    }
                }
            }
        }

        return cursor;
    }
}

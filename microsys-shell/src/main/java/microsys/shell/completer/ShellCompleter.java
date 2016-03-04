package microsys.shell.completer;

import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import microsys.shell.RegistrationManager;
import microsys.shell.model.Registration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Responsible for managing tab-completion within the shell.
 */
public class ShellCompleter implements Completer {
    @Nonnull
    private final CompletionTree completions;

    /**
     * @param registrationManager the {@link RegistrationManager} that is tracking the supported shell commands
     */
    public ShellCompleter(@Nonnull final RegistrationManager registrationManager) {
        this(Objects.requireNonNull(registrationManager).getRegistrations());
    }

    /**
     * @param registrations the {@link Registration} objects that define the available completions
     */
    public ShellCompleter(@Nonnull final Collection<Registration> registrations) {
        Objects.requireNonNull(registrations);

        this.completions = new CompletionTree();
        registrations.stream().map(Registration::getCompletions).forEach(this.completions::merge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int complete(@Nonnull final String buffer, final int cursor, @Nonnull final List<CharSequence> candidates) {
        final ArgumentCompleter.ArgumentList argumentList =
                new ArgumentCompleter.WhitespaceArgumentDelimiter().delimit(buffer, cursor);

        CompletionTree current = this.completions;
        if (argumentList.getArguments().length == 0) {
            candidates.addAll(current.getChildrenCandidates());
            return cursor;
        } else {
            final List<String> args = Arrays.asList(argumentList.getArguments());

            // Process all the previous arguments to get to the right spot in the completion tree.
            for (int i = 0; i < argumentList.getCursorArgumentIndex(); i++) {
                final Optional<CompletionTree> match = current.getChild(args.get(i));
                if (match.isPresent()) {
                    // User has already entered this tree node. Continue to the next argument.
                    current = match.get();
                } else {
                    // No idea what the user has typed, it is not recognized.
                    return cursor;
                }
            }

            // Process the last argument that needs to be tab-completed.
            final String arg = argumentList.getCursorArgumentIndex() >= args.size() ? "" :
                    args.get(argumentList.getCursorArgumentIndex());
            final List<CompletionTree> matches = current.getChildrenMatching(arg);

            if (matches.isEmpty()) {
                // No idea what the user typed, it is not recognized.
                return cursor;
            } else if (matches.size() == 1 && matches.get(0).getCompleter().isPresent()) {
                // Let the matching completer do the work.
                final Completer completer = matches.get(0).getCompleter().get();
                return cursor - arg.length() + completer.complete(arg, arg.length(), candidates);
            } else {
                matches.stream().map(CompletionTree::getCandidate).filter(Optional::isPresent).map(Optional
                        ::get).sorted().forEach(candidates::add);
                return cursor - arg.length();
            }
        }
    }
}

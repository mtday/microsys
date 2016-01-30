package microsys.shell;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.shell.model.Command;
import microsys.shell.model.CommandPath;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Responsible for managing all of the command registrations.
 */
public class RegistrationManager {
    private final static Logger LOG = LoggerFactory.getLogger(RegistrationManager.class);

    private final TreeMap<Registration, Command> registrations = new TreeMap<>();

    /**
     * @param shellEnvironment
     */
    public void loadCommands(final ShellEnvironment shellEnvironment) {
        final String packagePrefix = StringUtils.substringBeforeLast(getClass().getPackage().getName(), ".");
        final Reflections reflections = new Reflections(packagePrefix);

        reflections.getSubTypesOf(Command.class).stream().map(clazz -> createCommand(clazz, shellEnvironment))
                .filter(Optional::isPresent).map(Optional::get).forEach(command -> command.getRegistrations()
                .forEach(registration -> this.registrations.put(registration, command)));
    }

    protected Optional<Command> createCommand(
            final Class<? extends Command> commandClass, final ShellEnvironment shellEnvironment) {
        try {
            final Constructor<? extends Command> constructor =
                    commandClass.getDeclaredConstructor(ShellEnvironment.class);
            return Optional.of(constructor.newInstance(shellEnvironment));
        } catch (final NoSuchMethodException missingConstructor) {
            LOG.error("Shell command class is missing required constructor: " + commandClass.getName());
        } catch (final IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOG.error("Shell command class cannot be created: " + commandClass.getName());
        }
        return Optional.empty();
    }

    /**
     * @return the available registrations
     */
    public SortedSet<Registration> getRegistrations() {
        return new TreeSet<>(this.registrations.keySet());
    }

    /**
     * @param commandPath the {@link CommandPath} indicating the registrations to find
     * @return the available registrations matching the specified command path
     */
    public SortedSet<Registration> getRegistrations(final CommandPath commandPath) {
        final SortedSet<Registration> matching = new TreeSet<>();
        getRegistrations().stream().filter(registration -> registration.getPath().isPrefix(commandPath))
                .forEach(matching::add);
        return matching;
    }

    public Optional<Command> getCommand(final Registration registration) {
        return Optional.ofNullable(this.registrations.get(Objects.requireNonNull(registration)));
    }
}

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
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Nonnull;

/**
 * Responsible for managing all of the command registrations.
 */
public class RegistrationManager {
    private final static Logger LOG = LoggerFactory.getLogger(RegistrationManager.class);

    @Nonnull
    private final TreeMap<Registration, Command> registrations = new TreeMap<>();

    /**
     * @param shellEnvironment the {@link ShellEnvironment} providing the necessary objects used within the commands
     */
    public void loadCommands(@Nonnull final ShellEnvironment shellEnvironment) {
        Objects.requireNonNull(shellEnvironment);
        final String packagePrefix = StringUtils.substringBeforeLast(getClass().getPackage().getName(), ".");
        final Reflections reflections = new Reflections(packagePrefix);

        reflections.getSubTypesOf(Command.class).stream().map(clazz -> createCommand(clazz, shellEnvironment))
                .filter(Optional::isPresent).map(Optional::get).forEach(command -> command.getRegistrations()
                .forEach(registration -> this.registrations.put(registration, command)));
    }

    @Nonnull
    protected Optional<Command> createCommand(
            @Nonnull final Class<? extends Command> commandClass, @Nonnull final ShellEnvironment shellEnvironment) {
        Objects.requireNonNull(commandClass);
        Objects.requireNonNull(shellEnvironment);
        try {
            if (Modifier.isAbstract(commandClass.getModifiers())) {
                return Optional.empty();
            } else {
                final Constructor<? extends Command> constructor =
                        commandClass.getDeclaredConstructor(ShellEnvironment.class);
                return Optional.of(constructor.newInstance(shellEnvironment));
            }
        } catch (final NoSuchMethodException | IllegalAccessException | InstantiationException |
                InvocationTargetException failed) {
            LOG.error("Shell command class cannot be created: " + commandClass.getName());
        }
        return Optional.empty();
    }

    /**
     * @return the available registrations
     */
    @Nonnull
    public SortedSet<Registration> getRegistrations() {
        return new TreeSet<>(this.registrations.keySet());
    }

    /**
     * @param commandPath the {@link CommandPath} indicating the registrations to find
     * @return the available registrations matching the specified command path
     */
    @Nonnull
    public SortedSet<Registration> getRegistrations(@Nonnull final CommandPath commandPath) {
        Objects.requireNonNull(commandPath);
        final SortedSet<Registration> matching = new TreeSet<>();
        getRegistrations().stream().filter(registration -> registration.getPath().isPrefix(commandPath))
                .forEach(matching::add);
        return matching;
    }

    @Nonnull
    public Optional<Command> getCommand(@Nonnull final Registration registration) {
        return Optional.ofNullable(this.registrations.get(Objects.requireNonNull(registration)));
    }
}

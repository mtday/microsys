package microsys.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.crypto.CryptoFactory;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.discovery.impl.CuratorDiscoveryManager;
import microsys.shell.model.Command;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;
import okhttp3.OkHttpClient;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

/**
 * Perform testing on the {@link RegistrationManager} class.
 */
public class RegistrationManagerTest {
    @Test
    public void test() {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config = Mockito.mock(Config.class);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final RegistrationManager rm = new RegistrationManager();
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final CryptoFactory cryptoFactory = new DefaultCryptoFactory(config);
        final ShellEnvironment env =
                new ShellEnvironment(config, executor, cryptoFactory, curator, discovery, httpClient, rm);

        rm.loadCommands(env);

        final SortedSet<Registration> registrations = rm.getRegistrations();
        assertFalse(registrations.isEmpty());

        final SortedSet<Registration> help = rm.getRegistrations(new CommandPath("help"));
        assertFalse(help.isEmpty());
        assertEquals(1, help.size());

        final SortedSet<Registration> missing = rm.getRegistrations(new CommandPath("missing"));
        assertTrue(missing.isEmpty());

        final Optional<Command> helpCommand = rm.getCommand(help.first());
        assertTrue(helpCommand.isPresent());

        final Registration fakeRegistration = Mockito.mock(Registration.class);
        final Optional<Command> missingCommand = rm.getCommand(fakeRegistration);
        assertFalse(missingCommand.isPresent());
    }

    @Test
    public void testCreateCommandBadClass() {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Optional<Command> command =
                new RegistrationManager().createCommand(BrokenCommand.class, Mockito.mock(ShellEnvironment.class));
        assertFalse(command.isPresent());
    }

    @SuppressWarnings("unused")
    public static abstract class TestCommand extends Command {
        public TestCommand(@Nonnull final ShellEnvironment shellEnvironment) {
            super(shellEnvironment);
        }
    }

    private static class BrokenCommand extends Command {
        public BrokenCommand() {
            super(Mockito.mock(ShellEnvironment.class));
        }

        @Nonnull
        @Override
        public List<Registration> getRegistrations() {
            return Collections.emptyList();
        }

        @Nonnull
        @Override
        public CommandStatus process(
                @Nonnull UserCommand userCommand, @Nonnull PrintWriter writer) {
            return CommandStatus.SUCCESS;
        }
    }
}

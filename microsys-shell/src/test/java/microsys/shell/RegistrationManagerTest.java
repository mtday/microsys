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
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.model.Command;
import microsys.shell.model.CommandPath;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import okhttp3.OkHttpClient;

import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final RegistrationManager rm = new RegistrationManager();
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final ShellEnvironment env = new ShellEnvironment(config, executor, discovery, curator, rm, httpClient);

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

    public static abstract class TestCommand extends Command {
        public TestCommand(final ShellEnvironment shellEnvironment) {
            super(shellEnvironment);
        }
    }
}

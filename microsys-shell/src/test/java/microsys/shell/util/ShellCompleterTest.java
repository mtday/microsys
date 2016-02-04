package microsys.shell.util;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.curator.framework.CuratorFramework;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.RegistrationManager;
import microsys.shell.model.ShellEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * Perform testing on the {@link ShellCompleter} class.
 */
public class ShellCompleterTest {
    private static ShellCompleter shellCompleter;

    @BeforeClass
    public static void setup() throws Exception {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config = ConfigFactory.load();
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);

        shellCompleter = new ShellCompleter(registrationManager);
    }

    @Test
    public void testCompleteBufferEmpty() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("", 0, candidates);

        assertEquals(4, candidates.size());
        assertEquals(0, position);
        assertEquals("[exit, help, quit, service]", candidates.toString());
    }

    @Test
    public void testCompleteBufferWhiteSpace() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete(" ", 1, candidates);

        assertEquals(4, candidates.size());
        assertEquals(1, position);
        assertEquals("[exit, help, quit, service]", candidates.toString());
    }

    @Test
    public void testCompleteCommandSingleAvailable() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("hel", 3, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("help", candidates.get(0).toString());
    }

    @Test
    public void testCompleteCommandMultipleAvailable() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("serv", 4, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[service]", candidates.toString());
    }

    @Test
    public void testCompleteCommandCompleteMultipleAvailable() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service", 7, candidates);

        assertEquals(1, candidates.size());
        assertEquals(0, position);
        assertEquals("[service]", candidates.toString());
    }

    @Test
    public void testCompleteCommandCompleteMultipleAvailableWithSpace() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service ", 8, candidates);

        assertEquals(2, candidates.size());
        assertEquals(8, position);
        assertEquals("[list, restart]", candidates.toString());
    }

    @Test
    public void testCompleteCommandSingleAvailableWithOptionsNoWhiteSpace() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service list", 12, candidates);

        assertEquals(1, candidates.size());
        assertEquals(8, position);
        assertEquals("[list]", candidates.toString());
    }

    @Test
    public void testCompleteCommandSingleAvailableWithOptionsAndWhiteSpace() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service list ", 13, candidates);

        assertEquals(4, candidates.size());
        assertEquals(13, position);
        assertEquals("--host", candidates.get(0).toString());
        assertEquals("--type", candidates.get(1).toString());
        assertEquals("-h", candidates.get(2).toString());
        assertEquals("-t", candidates.get(3).toString());
    }

    @Test
    public void testCompleteCommandSingleAvailableWithDash() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service list -", 14, candidates);

        assertEquals(4, candidates.size());
        assertEquals(13, position);
        assertEquals("--host", candidates.get(0).toString());
        assertEquals("--type", candidates.get(1).toString());
        assertEquals("-h", candidates.get(2).toString());
        assertEquals("-t", candidates.get(3).toString());
    }

    @Test
    public void testCompleteCommandSingleAvailableWithTwoDashes() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service list --", 15, candidates);

        assertEquals(2, candidates.size());
        assertEquals(13, position);
        assertEquals("--host", candidates.get(0).toString());
        assertEquals("--type", candidates.get(1).toString());
    }

    @Test
    public void testCompleteCommandSingleAvailableWithParamPartial() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service list --ty", 17, candidates);

        assertEquals(1, candidates.size());
        assertEquals(13, position);
        assertEquals("--type", candidates.get(0).toString());
    }

    @Test
    public void testCompleteCommandSingleAvailableWithParamComplete() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service list --type", 19, candidates);

        assertEquals(1, candidates.size());
        assertEquals(13, position);
        assertEquals("--type", candidates.get(0).toString());
    }

    @Test
    public void testCompleteCommandSingleAvailableWithParamCompleteAndSpace() {
        final List<CharSequence> candidates = new ArrayList<>();
        final int position = shellCompleter.complete("service list --type ", 20, candidates);

        assertEquals(3, candidates.size());
        assertEquals(20, position);
        assertEquals("[config, health, web]", candidates.toString());
    }
}

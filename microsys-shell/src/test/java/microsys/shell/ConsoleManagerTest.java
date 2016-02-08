package microsys.shell;

import ch.qos.logback.classic.Level;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import microsys.common.config.CommonConfig;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.model.ShellEnvironment;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Perform testing on the {@link ConsoleManager} class
 */
public class ConsoleManagerTest {
    @Test
    public void testStandardConstructor() throws IOException {
        final Config config = ConfigFactory.load();
        final RegistrationManager registrationManager = new RegistrationManager();
        final ConsoleManager cm = new ConsoleManager(config, registrationManager);
        cm.stop();

        assertEquals(config, cm.getConfig());
        assertEquals(registrationManager, cm.getRegistrationManager());
        assertNotNull(cm.getConsoleReader());
    }

    @Test
    public void testRun() throws IOException {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config =
                ConfigFactory.parseString(String.format("%s = 1.2.3", CommonConfig.SYSTEM_VERSION.getKey()))
                        .withFallback(ConfigFactory.load());
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);

        final CapturingConsoleReader consoleReader =
                new CapturingConsoleReader("unrecognized", "", "  #comment", "help", "s", "service li -t", "'invalid");

        final ConsoleManager cm = new ConsoleManager(config, registrationManager, consoleReader);
        cm.run();

        assertTrue(consoleReader.isShutdown());

        final List<String> lines = consoleReader.getOutputLines();
        assertEquals(23, lines.size());

        int line = 0;
        // Startup
        assertEquals("\n", lines.get(line++));
        assertEquals("microsys 1.2.3", lines.get(line++));
        assertEquals("\n", lines.get(line++));
        assertEquals("Type 'help' to list the available commands", lines.get(line++));

        // unrecognized
        assertEquals("Unrecognized command: unrecognized", lines.get(line++));
        assertEquals("Use 'help' to see all the available commands.", lines.get(line++));

        // blank and #comment

        // help
        assertEquals("  exit                     exit the shell", lines.get(line++));
        assertEquals("  help                     display usage information for available shell commands",
                lines.get(line++));
        assertEquals("  quit                     exit the shell", lines.get(line++));
        assertEquals("  service control restart  request the restart of one or more services", lines.get(line++));
        assertEquals("  service control stop     request the stop of one or more services", lines.get(line++));
        assertEquals("  service list             provides information about the available services", lines.get(line++));
        assertEquals("  service memory           display memory usage information for one or more services",
                lines.get(line++));

        // s
        assertEquals("Showing help for commands that begin with: s", lines.get(line++));
        assertEquals("  service control restart  request the restart of one or more services", lines.get(line++));
        assertEquals("  service control stop     request the stop of one or more services", lines.get(line++));
        assertEquals("  service list             provides information about the available services", lines.get(line++));
        assertEquals("  service memory           display memory usage information for one or more services",
                lines.get(line++));

        // service li -t
        assertEquals("Assuming you mean: service list", lines.get(line++));
        assertEquals("Missing argument for option: t", lines.get(line++));

        // 'invalid
        assertEquals("--------------^", lines.get(line++));
        assertEquals("Missing terminating quote", lines.get(line++));

        // no more input
        assertEquals("\n", lines.get(line));
    }

    @Test
    public void testRunWithInterrupt() throws IOException {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config =
                ConfigFactory.parseString(String.format("%s = 1.2.3", CommonConfig.SYSTEM_VERSION.getKey()))
                        .withFallback(ConfigFactory.load());
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);

        final CapturingConsoleReader consoleReader = new CapturingConsoleReader("help");
        consoleReader.setInterrupt("");

        final ConsoleManager cm = new ConsoleManager(config, registrationManager, consoleReader);
        cm.run();
        assertTrue(consoleReader.isShutdown());

        final List<String> lines = consoleReader.getOutputLines();
        assertEquals(5, lines.size());

        int line = 0;
        // Startup
        assertEquals("\n", lines.get(line++));
        assertEquals("microsys 1.2.3", lines.get(line++));
        assertEquals("\n", lines.get(line++));
        assertEquals("Type 'help' to list the available commands", lines.get(line++));

        // no more input
        assertEquals("\n", lines.get(line));
    }

    @Test
    public void testRunWithInterruptPartial() throws IOException {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config =
                ConfigFactory.parseString(String.format("%s = 1.2.3", CommonConfig.SYSTEM_VERSION.getKey()))
                        .withFallback(ConfigFactory.load());
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);

        final CapturingConsoleReader consoleReader = new CapturingConsoleReader("help");
        consoleReader.setInterrupt("partial");

        final ConsoleManager cm = new ConsoleManager(config, registrationManager, consoleReader);
        cm.run();
        assertTrue(consoleReader.isShutdown());

        final List<String> lines = consoleReader.getOutputLines();
        assertEquals(12, lines.size());

        int line = 0;
        // Startup
        assertEquals("\n", lines.get(line++));
        assertEquals("microsys 1.2.3", lines.get(line++));
        assertEquals("\n", lines.get(line++));
        assertEquals("Type 'help' to list the available commands", lines.get(line++));

        // help
        assertEquals("  exit                     exit the shell", lines.get(line++));
        assertEquals("  help                     display usage information for available shell commands",
                lines.get(line++));
        assertEquals("  quit                     exit the shell", lines.get(line++));
        assertEquals("  service control restart  request the restart of one or more services", lines.get(line++));
        assertEquals("  service control stop     request the stop of one or more services", lines.get(line++));
        assertEquals("  service list             provides information about the available services", lines.get(line++));
        assertEquals("  service memory           display memory usage information for one or more services",
                lines.get(line++));

        // no more input
        assertEquals("\n", lines.get(line));
    }

    @Test
    public void testRunWithFileDoesNotExist() throws IOException {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final TemporaryFolder tmp = new TemporaryFolder();
        tmp.create();
        final File file = new File(tmp.getRoot(), "tmpfile.txt");

        try {
            final Config config =
                    ConfigFactory.parseString(String.format("%s = 1.2.3", CommonConfig.SYSTEM_VERSION.getKey()))
                            .withFallback(ConfigFactory.load());
            final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
            final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

            final RegistrationManager registrationManager = new RegistrationManager();
            final ShellEnvironment shellEnvironment =
                    new ShellEnvironment(config, discovery, curator, registrationManager);
            registrationManager.loadCommands(shellEnvironment);

            final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
            final ConsoleManager cm = new ConsoleManager(config, registrationManager, consoleReader);
            cm.run(file);
            assertTrue(consoleReader.isShutdown());

            final List<String> lines = consoleReader.getOutputLines();
            assertEquals(1, lines.size());
            assertEquals("\n", lines.get(0));
        } finally {
            tmp.delete();
        }
    }

    @Test
    public void testRunWithFile() throws IOException {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final TemporaryFolder tmp = new TemporaryFolder();
        tmp.create();
        final File file = tmp.newFile();
        try (final FileWriter fw = new FileWriter(file);
             final PrintWriter pw = new PrintWriter(fw, true)) {
            pw.println("help");
            pw.println("service list -t CONFIG");
        }

        try {
            final Config config =
                    ConfigFactory.parseString(String.format("%s = 1.2.3", CommonConfig.SYSTEM_VERSION.getKey()))
                            .withFallback(ConfigFactory.load());
            final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
            final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

            final RegistrationManager registrationManager = new RegistrationManager();
            final ShellEnvironment shellEnvironment =
                    new ShellEnvironment(config, discovery, curator, registrationManager);
            registrationManager.loadCommands(shellEnvironment);

            final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
            final ConsoleManager cm = new ConsoleManager(config, registrationManager, consoleReader);
            cm.run(file);
            assertTrue(consoleReader.isShutdown());

            final List<String> lines = consoleReader.getOutputLines();
            assertEquals(11, lines.size());

            int line = 0;
            // help
            assertEquals("# help", lines.get(line++));
            assertEquals("  exit                     exit the shell", lines.get(line++));
            assertEquals("  help                     display usage information for available shell commands",
                    lines.get(line++));
            assertEquals("  quit                     exit the shell", lines.get(line++));
            assertEquals("  service control restart  request the restart of one or more services", lines.get(line++));
            assertEquals("  service control stop     request the stop of one or more services", lines.get(line++));
            assertEquals("  service list             provides information about the available services",
                    lines.get(line++));
            assertEquals("  service memory           display memory usage information for one or more services",
                    lines.get(line++));
            assertEquals("# service list -t CONFIG", lines.get(line++));
            assertEquals("No services are running", lines.get(line++));

            // no more input
            assertEquals("\n", lines.get(line));
        } finally {
            tmp.delete();
        }
    }

    @Test
    public void testRunWithCommand() throws IOException {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final Config config =
                ConfigFactory.parseString(String.format("%s = 1.2.3", CommonConfig.SYSTEM_VERSION.getKey()))
                        .withFallback(ConfigFactory.load());
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);

        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();

        final ConsoleManager cm = new ConsoleManager(config, registrationManager, consoleReader);
        cm.run("help");
        assertTrue(consoleReader.isShutdown());

        final List<String> lines = consoleReader.getOutputLines();
        assertEquals(9, lines.size());

        int line = 0;
        // help
        assertEquals("# help", lines.get(line++));
        assertEquals("  exit                     exit the shell", lines.get(line++));
        assertEquals("  help                     display usage information for available shell commands",
                lines.get(line++));
        assertEquals("  quit                     exit the shell", lines.get(line++));
        assertEquals("  service control restart  request the restart of one or more services", lines.get(line++));
        assertEquals("  service control stop     request the stop of one or more services", lines.get(line++));
        assertEquals("  service list             provides information about the available services",
                lines.get(line++));
        assertEquals("  service memory           display memory usage information for one or more services",
                lines.get(line++));

        // no more input
        assertEquals("\n", lines.get(line));
    }

    @Test
    public void testCreateHistory() throws IOException {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        final String tmp = System.getProperty("java.io.tmpdir");
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("user.home", ConfigValueFactory.fromAnyRef(tmp));
        map.put(CommonConfig.SHELL_HISTORY_FILE.getKey(), ConfigValueFactory.fromAnyRef("history.txt"));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final String systemName = config.getString(CommonConfig.SYSTEM_NAME.getKey());

        // Make the directory where the history file is stored a file instead.
        final File file = new File(String.format("%s/.%s", tmp, systemName));
        if (file.exists()) {
            assertTrue("Failed to delete existing dir: " + file.getAbsolutePath(), file.delete());
        }

        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);

        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment = new ShellEnvironment(config, discovery, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);

        final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
        final ConsoleManager cm = new ConsoleManager(config, registrationManager, consoleReader);

        assertNotNull(cm.createHistory(config));
    }
}

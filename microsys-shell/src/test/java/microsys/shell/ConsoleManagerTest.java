package microsys.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import jline.Terminal;
import jline.console.ConsoleReader;
import microsys.common.config.CommonConfig;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.model.ShellEnvironment;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

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
                new CapturingConsoleReader("unrecognized", "", "  #comment", "help", "s", "service li -t",
                        "'invalid");

        final ConsoleManager cm = new ConsoleManager(config, registrationManager, consoleReader);
        cm.run();
        cm.stop();

        assertTrue(consoleReader.isShutdown());

        final List<String> lines = consoleReader.getOutputLines();
        assertEquals(21, lines.size());

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
        assertEquals("  exit             exit the shell", lines.get(line++));
        assertEquals("  help             display usage information for available shell commands", lines.get(line++));
        assertEquals("  quit             exit the shell", lines.get(line++));
        assertEquals("  service list     provides information about the available services", lines.get(line++));
        assertEquals("  service restart  request the restart of a service", lines.get(line++));

        // s
        assertEquals("Showing help for commands that begin with: s", lines.get(line++));
        assertEquals("  service list     provides information about the available services", lines.get(line++));
        assertEquals("  service restart  request the restart of a service", lines.get(line++));

        // service li -t
        assertEquals("Assuming you mean: service list", lines.get(line++));
        assertEquals("Missing argument for option: t", lines.get(line++));

        // 'invalid
        assertEquals("--------------^", lines.get(line++));
        assertEquals("Missing terminating quote", lines.get(line++));

        // no more input
        assertEquals("Received Ctrl-D", lines.get(line++));
        assertEquals("\n", lines.get(line));
    }

    private static class CapturingConsoleReader extends ConsoleReader {
        private final List<String> output = new LinkedList<>();
        private final List<String> lines = new LinkedList<>();

        private boolean shutdown = false;

        public CapturingConsoleReader(final String... lines) throws IOException {
            if (lines != null) {
                this.lines.addAll(Arrays.asList(lines));
            }
            super.shutdown();
        }

        @Override
        public PrintWriter getOutput() {
            return new PrintWriter(new StringWriter()) {
                @Override
                public void write(@Nonnull final String line, final int offset, final int length) {
                    if (!"\n".equals(line)) {
                        output.add(line);
                    }
                }
            };
        }

        @Override
        public String readLine() {
            if (lines.isEmpty()) {
                return null;
            }
            return lines.remove(0);
        }

        @Override
        public void print(final CharSequence s) {
            if (s != null) {
                this.output.add(s.toString());
            }
        }

        @Override
        public void println() {
            this.output.add("\n");
        }

        @Override
        public void println(final CharSequence line) {
            if (line != null) {
                this.output.add(line.toString());
            }
        }

        @Override
        public void shutdown() {
            this.shutdown = true;
        }

        @Override
        public Terminal getTerminal() {
            return Mockito.mock(Terminal.class);
        }

        public boolean isShutdown() {
            return this.shutdown;
        }

        public List<String> getOutputLines() {
            return this.output;
        }
    }
}

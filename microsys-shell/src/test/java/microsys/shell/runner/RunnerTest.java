package microsys.shell.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.common.config.CommonConfig;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.CapturingConsoleReader;
import microsys.shell.ConsoleManager;
import microsys.shell.RegistrationManager;
import microsys.shell.model.ShellEnvironment;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Perform testing on the {@link Runner} class.
 */
public class RunnerTest {
    @Test
    public void testRun() throws Exception {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        try (final TestingServer testServer = new TestingServer(true)) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(),
                    ConfigValueFactory.fromAnyRef(testServer.getConnectString()));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

            final ConsoleManager consoleManager = Mockito.mock(ConsoleManager.class);
            final Runner runner = new Runner(config);
            runner.setConsoleManager(consoleManager);
            runner.run();
            runner.shutdown();
        }
    }

    @Test
    public void testProcessCommandLineNoOptions() throws Exception {
        final Runner runner = Mockito.mock(Runner.class);
        Runner.processCommandLine(runner, new String[] {"shell"});
        Mockito.verify(runner).run();
        Mockito.verify(runner).shutdown();
    }

    @Test
    public void testProcessCommandLineWithNonExistentFile() throws Exception {
        final Runner runner = Mockito.mock(Runner.class);
        Runner.processCommandLine(runner, new String[] {"shell", "-f", "non-existent-file.txt"});
        Mockito.verify(runner, Mockito.times(0)).run(Mockito.any(File.class));
        Mockito.verify(runner).shutdown();
    }

    @Test
    public void testProcessCommandLineWithInvalidArgs() throws Exception {
        final Runner runner = Mockito.mock(Runner.class);
        Runner.processCommandLine(runner, new String[] {"shell", "-a"});
        Mockito.verify(runner, Mockito.times(0)).run(Mockito.any(File.class));
        Mockito.verify(runner).shutdown();
    }

    @Test
    public void testProcessCommandLineWithFile() throws Exception {
        final TemporaryFolder tmp = new TemporaryFolder();
        tmp.create();
        final File file = tmp.newFile();

        try {
            final Runner runner = Mockito.mock(Runner.class);
            Runner.processCommandLine(runner, new String[] {"shell", "-f", file.getAbsolutePath()});
            Mockito.verify(runner).run(Mockito.any(File.class));
            Mockito.verify(runner).shutdown();
        } finally {
            assertTrue(file.delete());
            tmp.delete();
        }
    }

    @Test
    public void testProcessCommandLineWithCommand() throws Exception {
        final ConsoleManager consoleManager = Mockito.mock(ConsoleManager.class);
        final Runner runner = Mockito.mock(Runner.class);
        Mockito.when(runner.getConsoleManager()).thenReturn(consoleManager);
        Mockito.doCallRealMethod().when(runner).run(Mockito.anyString());
        Runner.processCommandLine(runner, new String[] {"shell", "-c", "help"});
        Mockito.verify(runner).run(Mockito.anyString());
        Mockito.verify(runner).shutdown();
    }

    @Test(expected = Exception.class)
    public void testCreateCurator() throws Exception {
        final Runner runner = Mockito.mock(Runner.class);
        Mockito.when(runner.createCurator(Mockito.any())).thenCallRealMethod();

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef("localhost:12345"));
        runner.createCurator(ConfigFactory.parseMap(map).withFallback(ConfigFactory.load()));
    }

    @Test
    public void testRunWithFile() throws Exception {
        final TemporaryFolder tmp = new TemporaryFolder();
        tmp.create();
        final File file = tmp.newFile();
        try {
            try (final FileWriter fw = new FileWriter(file);
                 final PrintWriter pw = new PrintWriter(fw, true)) {
                pw.println("help");
                pw.println("service list -t CONFIG");
                pw.println("quit");
            }

            // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

            try (final TestingServer testServer = new TestingServer(true)) {
                final Map<String, ConfigValue> map = new HashMap<>();
                map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(),
                        ConfigValueFactory.fromAnyRef(testServer.getConnectString()));
                final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
                final ExecutorService executor = Executors.newFixedThreadPool(3);
                final CuratorFramework curator =
                        CuratorFrameworkFactory.builder().connectString(testServer.getConnectString()).namespace("test")
                                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
                curator.start();
                final DiscoveryManager discovery = new DiscoveryManager(config, curator);
                final RegistrationManager registrationManager = new RegistrationManager();
                final OkHttpClient httpClient = new OkHttpClient.Builder().build();
                final ShellEnvironment shellEnvironment =
                        new ShellEnvironment(config, executor, discovery, curator, registrationManager, httpClient);
                registrationManager.loadCommands(shellEnvironment);

                final CapturingConsoleReader consoleReader = new CapturingConsoleReader();
                final ConsoleManager consoleManager = new ConsoleManager(config, shellEnvironment, consoleReader);

                final Runner runner = new Runner(config);
                runner.setConsoleManager(consoleManager);
                Runner.processCommandLine(runner, new String[] {"shell", "-f", file.getAbsolutePath()});

                final List<String> o = consoleReader.getOutputLines();
                assertEquals(13, o.size());

                int i = 0;
                assertEquals("# help", o.get(i++));
                assertEquals("  exit                     exit the shell", o.get(i++));
                assertEquals("  help                     display usage information for available shell commands",
                        o.get(i++));
                assertEquals("  quit                     exit the shell", o.get(i++));
                assertEquals("  service control restart  request the restart of one or more services", o.get(i++));
                assertEquals("  service control stop     request the stop of one or more services", o.get(i++));
                assertEquals("  service list             provides information about the available services", o.get(i++));
                assertEquals("  service memory           display memory usage information for one or more services",
                        o.get(i++));
                assertEquals("# service list -t CONFIG", o.get(i++));
                assertEquals("No services are running", o.get(i++));
                assertEquals("# quit", o.get(i++));
                assertEquals("Terminating", o.get(i++));
                assertEquals("\n", o.get(i));
            }
        } finally {
            assertTrue(file.delete());
            tmp.delete();
        }
    }

    @Test
    public void testMainWithInvalidArgs() throws Exception {
        Runner.main("shell", "-a");
    }
}

package microsys.shell.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.common.config.CommonConfig;
import microsys.shell.ConsoleManager;
import microsys.shell.RegistrationManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing on the {@link Runner} class.
 */
public class RunnerTest {
    @Test
    public void test() throws Exception {
        // Don't want to see stack traces in the output when attempting to create invalid commands during testing.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RegistrationManager.class)).setLevel(Level.OFF);

        try (final TestingServer testServer = new TestingServer(true)) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(testServer.getConnectString()));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

            final ConsoleManager consoleManager = Mockito.mock(ConsoleManager.class);
            final Runner runner = new Runner(config);
            runner.setConsoleManager(consoleManager);
            runner.run();
            runner.shutdown();
        }
    }
}

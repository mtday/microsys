package microsys.shell.model;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.service.discovery.DiscoveryManager;
import microsys.shell.RegistrationManager;
import okhttp3.OkHttpClient;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Perform testing of the {@link Command} class.
 */
public class CommandTest {
    @Test
    public void test() {
        final Config config = Mockito.mock(Config.class);
        final ExecutorService executor = Mockito.mock(ExecutorService.class);
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final RegistrationManager registration = Mockito.mock(RegistrationManager.class);
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final ShellEnvironment env =
                new ShellEnvironment(config, executor, discovery, curator, registration, httpClient);

        final TestCommand testCommand = new TestCommand(env);
        assertEquals(env, testCommand.getShellEnvironment());
    }

    public static class TestCommand extends Command {
        public TestCommand(final ShellEnvironment env) {
            super(env);
        }

        @Override
        public List<Registration> getRegistrations() {
            return Collections.emptyList();
        }

        @Override
        public CommandStatus process(UserCommand userCommand, PrintWriter writer) {
            return CommandStatus.SUCCESS;
        }
    }
}

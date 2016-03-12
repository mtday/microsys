package microsys.shell.model;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.crypto.CryptoFactory;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.discovery.impl.CuratorDiscoveryManager;
import microsys.shell.RegistrationManager;
import okhttp3.OkHttpClient;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;

/**
 * Perform testing of the {@link Command} class.
 */
public class CommandTest {
    @Test
    public void test() {
        final Config config = Mockito.mock(Config.class);
        final ExecutorService executor = Mockito.mock(ExecutorService.class);
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final RegistrationManager registration = Mockito.mock(RegistrationManager.class);
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final CryptoFactory cryptoFactory = new DefaultCryptoFactory(config);
        final ShellEnvironment env =
                new ShellEnvironment(config, executor, cryptoFactory, curator, discovery, httpClient, registration);

        final TestCommand testCommand = new TestCommand(env);
        assertEquals(env, testCommand.getShellEnvironment());
    }

    public static class TestCommand extends Command {
        public TestCommand(@Nonnull final ShellEnvironment env) {
            super(env);
        }

        @Override
        @Nonnull
        public List<Registration> getRegistrations() {
            return Collections.emptyList();
        }

        @Override
        @Nonnull
        public CommandStatus process(@Nonnull UserCommand userCommand, @Nonnull PrintWriter writer) {
            return CommandStatus.SUCCESS;
        }
    }
}

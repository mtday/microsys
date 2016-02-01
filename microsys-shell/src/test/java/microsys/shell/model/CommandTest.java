package microsys.shell.model;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.service.discovery.DiscoveryManager;
import microsys.shell.RegistrationManager;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * Perform testing of the {@link Command} class.
 */
public class CommandTest {
    @Test
    public void test() {
        final Config config = Mockito.mock(Config.class);
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final RegistrationManager registration = Mockito.mock(RegistrationManager.class);
        final ShellEnvironment env = new ShellEnvironment(config, discovery, curator, registration);

        final TestCommand testCommand = new TestCommand(env);
        assertEquals(env, testCommand.getShellEnvironment());
    }

    private static class TestCommand extends Command {
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

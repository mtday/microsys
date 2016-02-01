package microsys.shell.model;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.service.discovery.DiscoveryManager;
import microsys.shell.RegistrationManager;

/**
 * Perform testing on the {@link ShellEnvironment}.
 */
public class ShellEnvironmentTest {
    @Test
    public void test() {
        final Config config = Mockito.mock(Config.class);
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final RegistrationManager registration = Mockito.mock(RegistrationManager.class);

        final ShellEnvironment env = new ShellEnvironment(config, discovery, curator, registration);

        assertEquals(config, env.getConfig());
        assertEquals(discovery, env.getDiscoveryManager());
        assertEquals(curator, env.getCuratorFramework());
        assertEquals(registration, env.getRegistrationManager());
    }
}

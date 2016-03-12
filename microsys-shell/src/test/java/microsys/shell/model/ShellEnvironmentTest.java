package microsys.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.crypto.CryptoFactory;
import microsys.discovery.impl.CuratorDiscoveryManager;
import microsys.shell.RegistrationManager;
import okhttp3.OkHttpClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Perform testing on the {@link ShellEnvironment}.
 */
public class ShellEnvironmentTest {
    @Test
    public void test() {
        final Config config = Mockito.mock(Config.class);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        final CryptoFactory cryptoFactory = Mockito.mock(CryptoFactory.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final RegistrationManager registration = Mockito.mock(RegistrationManager.class);

        final ShellEnvironment env =
                new ShellEnvironment(config, executor, cryptoFactory, curator, discovery, httpClient, registration);

        assertEquals(config, env.getConfig());
        assertEquals(executor, env.getExecutor());
        assertEquals(cryptoFactory, env.getCryptoFactory());
        assertEquals(curator, env.getCuratorFramework());
        assertEquals(discovery, env.getDiscoveryManager());
        assertEquals(httpClient, env.getHttpClient());
        assertEquals(registration, env.getRegistrationManager());

        assertNotNull(env.getServiceClient());
        assertNotNull(env.getConfigClient());
    }
}

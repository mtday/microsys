package microsys.shell.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.crypto.CryptoFactory;
import microsys.service.discovery.DiscoveryManager;
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
        final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final RegistrationManager registration = Mockito.mock(RegistrationManager.class);
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final CryptoFactory cryptoFactory = Mockito.mock(CryptoFactory.class);

        final ShellEnvironment env =
                new ShellEnvironment(config, executor, discovery, curator, registration, httpClient, cryptoFactory);

        assertEquals(config, env.getConfig());
        assertEquals(discovery, env.getDiscoveryManager());
        assertEquals(curator, env.getCuratorFramework());
        assertEquals(registration, env.getRegistrationManager());
        assertEquals(httpClient, env.getHttpClient());
        assertEquals(cryptoFactory, env.getCryptoFactory());

        assertNotNull(env.getServiceClient());
        assertNotNull(env.getConfigClient());
    }
}

package microsys.service.model;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.model.service.ServiceType;
import microsys.crypto.CryptoFactory;
import microsys.discovery.impl.CuratorDiscoveryManager;
import okhttp3.OkHttpClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Perform testing on the {@link ServiceEnvironment}.
 */
public class ServiceEnvironmentTest {
    @Test
    public void test() {
        final Config config = Mockito.mock(Config.class);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final CryptoFactory cryptoFactory = Mockito.mock(CryptoFactory.class);

        final ServiceEnvironment env =
                new ServiceEnvironment(config, ServiceType.SHELL, executor, cryptoFactory, curator, discovery,
                        httpClient);

        assertEquals(config, env.getConfig());
        assertEquals(discovery, env.getDiscoveryManager());
        assertEquals(curator, env.getCuratorFramework());
        assertEquals(httpClient, env.getHttpClient());
        assertEquals(cryptoFactory, env.getCryptoFactory());
    }
}

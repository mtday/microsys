package microsys.config.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.common.config.CommonConfig;
import microsys.common.model.ServiceType;
import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.config.runner.Runner;
import microsys.config.service.CuratorConfigService;
import microsys.crypto.CryptoFactory;
import microsys.service.BaseService;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.model.Service;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import spark.webserver.JettySparkServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing of the {@link ConfigClient} class.
 */
public class ConfigClientIT {
    private static TestingServer testingServer;
    private static ExecutorService executor;
    private static CuratorFramework curator;
    private static DiscoveryManager discovery;
    private static CryptoFactory crypto;
    private static Runner runner;
    private static OkHttpClient httpClient;
    private static MockWebServer mockServer;

    @BeforeClass
    public static void before() throws Exception {
        // Don't want to see too much logging output.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CuratorConfigService.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RequestLoggingFilter.class)).setLevel(Level.OFF);
        LogManager.getLogManager().reset(); // Turn off logging from MockWebServer

        testingServer = new TestingServer();

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(testingServer.getConnectString()));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

        executor = Executors.newFixedThreadPool(3);
        curator =
                CuratorFrameworkFactory.builder().namespace("namespace").connectString(testingServer.getConnectString())
                        .defaultData(new byte[0]).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        discovery = new DiscoveryManager(config, curator);
        crypto = new CryptoFactory(config);
        runner = new Runner(config, executor, curator, discovery, crypto);

        // Wait for the server to start.
        TimeUnit.MILLISECONDS.sleep(500);

        httpClient =
                new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).retryOnConnectionFailure(false).build();

        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterClass
    public static void after() throws Exception {
        if (mockServer != null) {
            mockServer.shutdown();
        }
        if (runner != null) {
            runner.stop();
        }
        if (executor != null) {
            executor.shutdown();
        }
        if (discovery != null) {
            discovery.close();
        }
        if (curator != null) {
            curator.close();
        }
        if (testingServer != null) {
            testingServer.close();
        }
    }

    @Test
    public void test() throws Exception {
        final ConfigClient client = new ConfigClient(executor, discovery, httpClient);

        final ConfigKeyValueCollection coll = client.getAll().get();
        assertEquals(0, coll.size());

        assertFalse(client.set(new ConfigKeyValue("key", "value")).get().isPresent());

        // Wait a little to allow the value to be stored.
        TimeUnit.MILLISECONDS.sleep(300);

        final ConfigKeyValueCollection afterSet = client.getAll().get();
        assertEquals(1, afterSet.size());

        // Overwrite the previous value.
        final Optional<ConfigKeyValue> oldValue = client.set(new ConfigKeyValue("key", "new-value")).get();
        assertTrue(oldValue.isPresent());
        assertEquals(new ConfigKeyValue("key", "value"), oldValue.get());

        // Wait a little to allow the value to be stored.
        TimeUnit.MILLISECONDS.sleep(300);

        final Optional<ConfigKeyValue> get = client.get("key").get();
        assertTrue(get.isPresent());
        assertEquals(new ConfigKeyValue("key", "new-value"), get.get());

        final Optional<ConfigKeyValue> unset = client.unset("key").get();
        assertTrue(unset.isPresent());
        assertEquals(new ConfigKeyValue("key", "new-value"), unset.get());

        final Optional<ConfigKeyValue> getMissing = client.get("missing").get();
        assertFalse(getMissing.isPresent());

        final Optional<ConfigKeyValue> unsetMissing = client.unset("missing").get();
        assertFalse(unsetMissing.isPresent());
    }

    @Test(expected = ExecutionException.class)
    public void testNoConfigServiceGetAll() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.empty());

        final ConfigClient client = new ConfigClient(executor, mockDiscovery, httpClient);
        client.getAll().get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoConfigServiceGet() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.empty());

        final ConfigClient client = new ConfigClient(executor, mockDiscovery, httpClient);
        client.get("key").get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoConfigServiceSet() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.empty());

        final ConfigClient client = new ConfigClient(executor, mockDiscovery, httpClient);
        client.set(new ConfigKeyValue("key", "value")).get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoConfigServiceUnset() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.empty());

        final ConfigClient client = new ConfigClient(executor, mockDiscovery, httpClient);
        client.unset("key").get();
    }

    @Test(expected = ExecutionException.class)
    public void testGetAllInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.of(
                new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(), false, "1.2.3")));

        final ConfigClient client = new ConfigClient(executor, mockDiscovery, httpClient);
        client.getAll().get();
    }

    @Test(expected = ExecutionException.class)
    public void testGetInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.of(
                new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(), false, "1.2.3")));

        final ConfigClient client = new ConfigClient(executor, mockDiscovery, httpClient);
        client.get("key").get();
    }

    @Test(expected = ExecutionException.class)
    public void testSetInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.of(
                new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(), false, "1.2.3")));

        final ConfigClient client = new ConfigClient(executor, mockDiscovery, httpClient);
        client.set(new ConfigKeyValue("key", "value")).get();
    }

    @Test(expected = ExecutionException.class)
    public void testUnsetInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.of(
                new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(), false, "1.2.3")));

        final ConfigClient client = new ConfigClient(executor, mockDiscovery, httpClient);
        client.unset("key").get();
    }
}

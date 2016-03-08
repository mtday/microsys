package microsys.security.client;

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
import microsys.crypto.CryptoFactory;
import microsys.security.model.User;
import microsys.security.runner.Runner;
import microsys.service.BaseService;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.model.Service;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import spark.webserver.JettySparkServer;

import java.util.Arrays;
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
 * Perform testing of the {@link SecurityClient} class.
 */
public class SecurityClientIT {
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
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RequestLoggingFilter.class)).setLevel(Level.OFF);
        LogManager.getLogManager().reset(); // Turn off logging from MockWebServer

        testingServer = new TestingServer();

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(testingServer.getConnectString()));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

        executor = Executors.newFixedThreadPool(3);
        curator = CuratorFrameworkFactory.builder().namespace("sec-client")
                .connectString(testingServer.getConnectString()).defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
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
        final SecurityClient client = new SecurityClient(executor, discovery, httpClient);

        final Optional<User> byId = client.getById("id").get();
        assertFalse(byId.isPresent());

        final Optional<User> byName = client.getByName("name").get();
        assertFalse(byName.isPresent());

        final Optional<User> missing = client.remove("id").get();
        assertFalse(missing.isPresent());

        final User user = new User("id", "name", Arrays.asList("A", "B"));
        final Optional<User> save = client.save(user).get();
        assertFalse(save.isPresent());

        final Optional<User> byIdExists = client.getById(user.getId()).get();
        assertTrue(byIdExists.isPresent());
        assertEquals(user, byIdExists.get());

        final Optional<User> byNameExists = client.getByName(user.getUserName()).get();
        assertTrue(byNameExists.isPresent());
        assertEquals(user, byNameExists.get());

        final User updatedUser = new User(user.getId(), user.getUserName() + "2", Arrays.asList("A", "B", "C"));
        final Optional<User> save2 = client.save(updatedUser).get();
        assertTrue(save2.isPresent());
        assertEquals(user, save2.get());

        final Optional<User> byIdUpdated = client.getById(updatedUser.getId()).get();
        assertTrue(byIdUpdated.isPresent());
        assertEquals(updatedUser, byIdUpdated.get());

        final Optional<User> byNameUpdated = client.getByName(updatedUser.getUserName()).get();
        assertTrue(byNameUpdated.isPresent());
        assertEquals(updatedUser, byNameUpdated.get());

        final Optional<User> removed = client.remove(updatedUser.getId()).get();
        assertTrue(removed.isPresent());
        assertEquals(updatedUser, removed.get());
    }

    @Test(expected = ExecutionException.class)
    public void testNoSecurityServiceGetById() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.empty());

        final SecurityClient client = new SecurityClient(executor, mockDiscovery, httpClient);
        client.getById("id").get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoSecurityServiceGetByName() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.empty());

        final SecurityClient client = new SecurityClient(executor, mockDiscovery, httpClient);
        client.getByName("name").get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoSecurityServiceSave() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.empty());

        final SecurityClient client = new SecurityClient(executor, mockDiscovery, httpClient);
        client.save(new User("id", "name", Arrays.asList("A", "B"))).get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoSecurityServiceUnset() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.empty());

        final SecurityClient client = new SecurityClient(executor, mockDiscovery, httpClient);
        client.remove("id").get();
    }

    @Test(expected = ExecutionException.class)
    public void testGetByIdInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.of(
                new Service(ServiceType.SECURITY, mockServer.getHostName(), mockServer.getPort(), false, "1.2.3")));

        final SecurityClient client = new SecurityClient(executor, mockDiscovery, httpClient);
        client.getById("id").get();
    }

    @Test(expected = ExecutionException.class)
    public void testGetByNameInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.of(
                new Service(ServiceType.SECURITY, mockServer.getHostName(), mockServer.getPort(), false, "1.2.3")));

        final SecurityClient client = new SecurityClient(executor, mockDiscovery, httpClient);
        client.getByName("name").get();
    }

    @Test(expected = ExecutionException.class)
    public void testSaveInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.of(
                new Service(ServiceType.SECURITY, mockServer.getHostName(), mockServer.getPort(), false, "1.2.3")));

        final SecurityClient client = new SecurityClient(executor, mockDiscovery, httpClient);
        client.save(new User("id", "name", Arrays.asList("A", "B"))).get();
    }

    @Test(expected = ExecutionException.class)
    public void testUnsetInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.of(
                new Service(ServiceType.SECURITY, mockServer.getHostName(), mockServer.getPort(), false, "1.2.3")));

        final SecurityClient client = new SecurityClient(executor, mockDiscovery, httpClient);
        client.remove("id").get();
    }
}

package microsys.security.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.common.config.ConfigKeys;
import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.discovery.DiscoveryManager;
import microsys.security.model.User;
import microsys.security.runner.Runner;
import microsys.service.BaseService;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.model.ServiceEnvironment;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import spark.webserver.JettySparkServer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing of the {@link SecurityClient} class.
 */
public class SecurityClientIT {
    private static TestingServer testingServer;
    private static Runner runner;
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
        map.put(ConfigKeys.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(testingServer.getConnectString()));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

        runner = new Runner(config, new CountDownLatch(1));

        // Wait for the server to start.
        TimeUnit.MILLISECONDS.sleep(500);

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
        if (testingServer != null) {
            testingServer.close();
        }
    }

    @Test
    public void test() throws Exception {
        final SecurityClient client = new SecurityClient(runner.getServiceEnvironment());

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

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getDiscoveryManager()).thenReturn(mockDiscovery);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(new OkHttpClient.Builder().build());

        final SecurityClient client = new SecurityClient(serviceEnvironment);
        client.getById("id").get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoSecurityServiceGetByName() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.empty());

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getDiscoveryManager()).thenReturn(mockDiscovery);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(new OkHttpClient.Builder().build());

        final SecurityClient client = new SecurityClient(serviceEnvironment);
        client.getByName("name").get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoSecurityServiceSave() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.empty());

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getDiscoveryManager()).thenReturn(mockDiscovery);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(new OkHttpClient.Builder().build());

        final SecurityClient client = new SecurityClient(serviceEnvironment);
        client.save(new User("id", "name", Arrays.asList("A", "B"))).get();
    }

    @Test(expected = ExecutionException.class)
    public void testNoSecurityServiceUnset() throws Exception {
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.SECURITY)).thenReturn(Optional.empty());

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getDiscoveryManager()).thenReturn(mockDiscovery);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(new OkHttpClient.Builder().build());

        final SecurityClient client = new SecurityClient(serviceEnvironment);
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

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getDiscoveryManager()).thenReturn(mockDiscovery);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(new OkHttpClient.Builder().build());

        final SecurityClient client = new SecurityClient(serviceEnvironment);
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

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getDiscoveryManager()).thenReturn(mockDiscovery);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(new OkHttpClient.Builder().build());

        final SecurityClient client = new SecurityClient(serviceEnvironment);
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

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getDiscoveryManager()).thenReturn(mockDiscovery);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(new OkHttpClient.Builder().build());

        final SecurityClient client = new SecurityClient(serviceEnvironment);
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

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getDiscoveryManager()).thenReturn(mockDiscovery);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(Executors.newFixedThreadPool(1));
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(new OkHttpClient.Builder().build());

        final SecurityClient client = new SecurityClient(serviceEnvironment);
        client.remove("id").get();
    }
}

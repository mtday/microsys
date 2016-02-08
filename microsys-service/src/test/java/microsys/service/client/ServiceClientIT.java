package microsys.service.client;

import ch.qos.logback.classic.Level;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import microsys.common.config.CommonConfig;
import microsys.common.model.ServiceType;
import microsys.service.BaseService;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.model.Service;
import microsys.service.model.ServiceMemory;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import spark.webserver.JettySparkServer;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Perform testing of the {@link ServiceClient} class.
 */
public class ServiceClientIT {
    private static TestingServer testingServer;
    private static ExecutorService executor;
    private static CuratorFramework curator;
    private static DiscoveryManager discovery;
    private static OkHttpClient httpClient;
    private static MockWebServer mockServer;
    private static BaseService baseService;

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

        executor = Executors.newFixedThreadPool(4);
        curator =
                CuratorFrameworkFactory.builder().namespace("ns").connectString(testingServer.getConnectString())
                        .defaultData(new byte[0]).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        discovery = new DiscoveryManager(config, curator);
        baseService = new BaseService(config, executor, curator, discovery, ServiceType.CONFIG) {
        };

        // Wait for the server to start.
        TimeUnit.MILLISECONDS.sleep(500);

        httpClient = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).retryOnConnectionFailure(false).build();

        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterClass
    public static void after() throws Exception {
        if (mockServer != null) {
            mockServer.shutdown();
        }
        if (baseService != null) {
            baseService.stop();
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
        final ServiceClient client = new ServiceClient(executor, httpClient);

        final Optional<Service> service = baseService.getService();
        assertTrue(service.isPresent());

        final ServiceMemory memory = client.getMemory(service.get()).get();
        assertNotNull(memory);

        final Map<Service, ServiceMemory> map = client.getMemory(Collections.singletonList(service.get())).get();
        assertNotNull(map);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(service.get()));
    }

    @Test(expected = ExecutionException.class)
    public void testMemoryInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.of(service));

        final ServiceClient client = new ServiceClient(executor, httpClient);
        client.getMemory(service).get();
    }

    @Test(expected = ExecutionException.class)
    public void testMemoryMapInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");
        final DiscoveryManager mockDiscovery = Mockito.mock(DiscoveryManager.class);
        Mockito.when(mockDiscovery.getRandom(ServiceType.CONFIG)).thenReturn(Optional.of(service));

        final ServiceClient client = new ServiceClient(executor, httpClient);
        client.getMemory(Collections.singletonList(service)).get();
    }
}

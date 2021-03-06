package microsys.service.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import microsys.common.config.ConfigKeys;
import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.crypto.CryptoFactory;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.discovery.DiscoveryManager;
import microsys.discovery.impl.CuratorDiscoveryManager;
import microsys.service.BaseService;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.model.ServiceControlStatus;
import microsys.service.model.ServiceEnvironment;
import microsys.service.model.ServiceInfo;
import microsys.service.model.ServiceMemory;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import spark.webserver.JettySparkServer;

import java.util.Collections;
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
 * Perform testing of the {@link ServiceClient} class.
 */
public class ServiceClientIT {
    private static TestingServer testingServer;
    private static Config config;
    private static ExecutorService executor;
    private static CuratorFramework curator;
    private static DiscoveryManager discovery;
    private static CryptoFactory crypto;
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
        map.put(ConfigKeys.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(testingServer.getConnectString()));
        map.put(ConfigKeys.SYSTEM_NAME.getKey(), ConfigValueFactory.fromAnyRef("system-name"));
        map.put(ConfigKeys.SYSTEM_VERSION.getKey(), ConfigValueFactory.fromAnyRef("1.2.3"));
        config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

        executor = Executors.newFixedThreadPool(4);
        curator =
                CuratorFrameworkFactory.builder().namespace("ns").connectString(testingServer.getConnectString())
                        .defaultData(new byte[0]).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        discovery = new CuratorDiscoveryManager(config, curator);
        crypto = new DefaultCryptoFactory(config);
        httpClient = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).retryOnConnectionFailure(false).build();

        baseService = new BaseService(config, ServiceType.CONFIG, executor, crypto, curator, discovery, httpClient) {
        };

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
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final ServiceClient client = new ServiceClient(serviceEnvironment);

        final Optional<Service> service = baseService.getService();
        assertTrue(service.isPresent());

        final ServiceInfo info = client.getInfo(service.get()).get();
        assertNotNull(info);
        assertEquals(new ServiceInfo(ServiceType.CONFIG, "system-name", "1.2.3"), info);

        final Map<Service, ServiceInfo> infoMap = client.getInfo(Collections.singletonList(service.get())).get();
        assertNotNull(infoMap);
        assertEquals(1, infoMap.size());
        assertTrue(infoMap.containsKey(service.get()));
        assertEquals(new ServiceInfo(ServiceType.CONFIG, "system-name", "1.2.3"), infoMap.get(service.get()));

        final ServiceMemory memory = client.getMemory(service.get()).get();
        assertNotNull(memory);

        final Map<Service, ServiceMemory> memMap = client.getMemory(Collections.singletonList(service.get())).get();
        assertNotNull(memMap);
        assertEquals(1, memMap.size());
        assertTrue(memMap.containsKey(service.get()));
    }

    @Test
    public void testControlStop() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_OK);
        response.setBody(new ServiceControlStatus(true, "stop").toJson().toString());
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.stop(service).get();
    }

    @Test
    public void testControlStopCollection() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_OK);
        response.setBody(new ServiceControlStatus(true, "stop").toJson().toString());
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.stop(Collections.singletonList(service)).get();
    }

    @Test
    public void testControlRestart() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_OK);
        response.setBody(new ServiceControlStatus(true, "restart").toJson().toString());
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.restart(service).get();
    }

    @Test
    public void testControlRestartCollection() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_OK);
        response.setBody(new ServiceControlStatus(true, "restart").toJson().toString());
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.restart(Collections.singletonList(service)).get();
    }

    @Test(expected = ExecutionException.class)
    public void testControlStopInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.stop(service).get();
    }

    @Test(expected = ExecutionException.class)
    public void testControlStopCollectionInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.stop(Collections.singletonList(service)).get();
    }

    @Test(expected = ExecutionException.class)
    public void testControlRestartInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.restart(service).get();
    }

    @Test(expected = ExecutionException.class)
    public void testControlRestartCollectionInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.restart(Collections.singletonList(service)).get();
    }

    @Test(expected = ExecutionException.class)
    public void testMemoryInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.getMemory(service).get();
    }

    @Test(expected = ExecutionException.class)
    public void testMemoryMapInvalidResponse() throws Exception {
        final MockResponse response = new MockResponse();
        response.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        mockServer.enqueue(response);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getHttpClient()).thenReturn(httpClient);
        Mockito.when(serviceEnvironment.getCryptoFactory()).thenReturn(crypto);

        final Service service = new Service(ServiceType.CONFIG, mockServer.getHostName(), mockServer.getPort(),
                false, "1.2.3");

        final ServiceClient client = new ServiceClient(serviceEnvironment);
        client.getMemory(Collections.singletonList(service)).get();
    }
}

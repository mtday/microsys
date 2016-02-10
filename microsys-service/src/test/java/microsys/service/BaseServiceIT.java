package microsys.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.common.config.CommonConfig;
import microsys.common.model.ServiceType;
import microsys.service.discovery.DiscoveryException;
import microsys.service.discovery.DiscoveryManager;
import spark.webserver.JettySparkServer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Perform testing of the {@link BaseService} class.
 */
public class BaseServiceIT {
    @Test
    public void test() throws Exception {
        // Don't want to see too much logging output.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);

        BaseService baseService = null;
        try (final TestingServer zookeeper = new TestingServer()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zookeeper.getConnectString()));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

            baseService = new BaseService(config, ServiceType.CONFIG, new CountDownLatch(1)) {
            };

            assertEquals(config, baseService.getConfig());
            assertNotNull(baseService.getExecutor());

            // Sleep a little to wait for the spark service to start and register with service discovery.
            TimeUnit.MILLISECONDS.sleep(500);

            baseService.stop();

            assertFalse(baseService.getShouldRestart());
            baseService.setShouldRestart(true);
            assertTrue(baseService.getShouldRestart());
            baseService.setShouldRestart(false);
        } finally {
            if (baseService != null) {
                // Shouldn't cause a problem to call stop twice.
                baseService.stop();
            }
        }
    }

    @Test
    public void testParameterConstructor() throws Exception {
        // Don't want to see too much logging output.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);

        BaseService baseService = null;
        try (final TestingServer zookeeper = new TestingServer()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zookeeper.getConnectString()));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

            final CuratorFramework curator = CuratorFrameworkFactory.builder().namespace("base-service")
                    .connectString(zookeeper.getConnectString()).defaultData(new byte[0])
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
            curator.start();

            final ExecutorService executor = Executors.newFixedThreadPool(2);
            final DiscoveryManager discovery = new DiscoveryManager(config, curator);

            baseService = new BaseService(config, executor, curator, discovery, ServiceType.CONFIG) {
            };

            assertEquals(config, baseService.getConfig());
            assertNotNull(baseService.getExecutor());

            discovery.close();
            executor.shutdown();
            curator.close();
        } finally {
            if (baseService != null) {
                baseService.stop();
            }
        }
    }

    @Test
    public void testDiscoveryManagerExceptions() throws Exception {
        // Don't want to see too much logging output.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);

        BaseService baseService = null;
        try (final TestingServer zookeeper = new TestingServer()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zookeeper.getConnectString()));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

            final CuratorFramework curator = CuratorFrameworkFactory.builder().namespace("discovery")
                    .connectString(zookeeper.getConnectString()).defaultData(new byte[0])
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
            curator.start();

            final ExecutorService executor = Executors.newFixedThreadPool(2);
            final DiscoveryManager discovery = Mockito.mock(DiscoveryManager.class);
            Mockito.doThrow(new DiscoveryException("Fake")).when(discovery).register(Mockito.any());
            Mockito.doThrow(new DiscoveryException("Fake")).when(discovery).unregister(Mockito.any());

            baseService = new BaseService(config, executor, curator, discovery, ServiceType.CONFIG) {
            };

            assertEquals(config, baseService.getConfig());
            assertNotNull(baseService.getExecutor());

            // Sleep a little to wait for the spark service to start and register with service discovery.
            TimeUnit.MILLISECONDS.sleep(500);

            discovery.close();
            executor.shutdown();
            curator.close();
        } finally {
            if (baseService != null) {
                baseService.stop();
            }
        }
    }

    @Test(expected = Exception.class)
    public void testFailureConnectingToZookeeper() throws Exception {
        // Don't want to see too much logging output.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef("localhost:12345"));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

        // This will fail to connect to zookeeper and result in an exception.
        new BaseService(config, ServiceType.CONFIG, new CountDownLatch(1)) {
        };
    }

    /**
     * Need to ignore this. When the static Spark.secure is called, we can no longer connect insecurely to
     * any Spark services, and there is no way to "unsecure" Spark afterwards, which breaks unit tests.
     */
    @Ignore
    public void testSecureService() throws Exception {
        // Don't want to see too much logging output.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);

        final File keystore = new File("src/test/resources/keystore.jks");
        final File truststore = new File("src/test/resources/truststore.jks");

        BaseService baseService = null;
        try (final TestingServer zookeeper = new TestingServer()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(CommonConfig.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(CommonConfig.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.getAbsolutePath()));
            map.put(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
            map.put(CommonConfig.SSL_TRUSTSTORE_FILE.getKey(),
                    ConfigValueFactory.fromAnyRef(truststore.getAbsolutePath()));
            map.put(CommonConfig.SSL_TRUSTSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
            map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zookeeper.getConnectString()));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

            baseService = new BaseService(config, ServiceType.CONFIG, new CountDownLatch(1)) {
            };

            assertEquals(config, baseService.getConfig());
            assertNotNull(baseService.getExecutor());
        } finally {
            if (baseService != null) {
                baseService.stop();
            }
        }
    }
}

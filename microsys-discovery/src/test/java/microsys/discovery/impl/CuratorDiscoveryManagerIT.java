package microsys.discovery.impl;

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
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.config.ConfigKeys;
import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.discovery.DiscoveryException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing on the {@link CuratorDiscoveryManager} class.
 */
public class CuratorDiscoveryManagerIT {
    private static Config config;
    private static TestingServer testingServer;
    private static CuratorFramework curator;

    @BeforeClass
    public static void setup() throws Exception {
        testingServer = new TestingServer();

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(testingServer.getConnectString()));
        config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

        curator =
                CuratorFrameworkFactory.builder().namespace("namespace").connectString(testingServer.getConnectString())
                        .defaultData(new byte[0]).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (curator != null) {
            curator.close();
        }
        if (testingServer != null) {
            testingServer.close();
        }
    }

    @Test
    public void test() throws Exception {
        final CuratorDiscoveryManager discovery = new CuratorDiscoveryManager(config, curator);
        try {
            Assert.assertEquals(config, discovery.getConfig());
            assertNotNull(discovery.getDiscovery());

            // Nothing registered at first.
            Assert.assertEquals(0, discovery.getAll().size());
            Assert.assertEquals(0, discovery.getAll(ServiceType.CONFIG).size());
            assertFalse(discovery.getRandom(ServiceType.CONFIG).isPresent());

            final Service service = new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3");
            discovery.register(service);

            // Should now have one registered service.
            Assert.assertEquals(1, discovery.getAll().size());
            Assert.assertEquals(1, discovery.getAll(ServiceType.CONFIG).size());
            assertTrue(discovery.getRandom(ServiceType.CONFIG).isPresent());

            discovery.unregister(service);

            // Nothing registered at again.
            Assert.assertEquals(0, discovery.getAll().size());
            Assert.assertEquals(0, discovery.getAll(ServiceType.CONFIG).size());
            assertFalse(discovery.getRandom(ServiceType.CONFIG).isPresent());
        } finally {
            discovery.close();
            assertTrue(discovery.isClosed());
        }

        // Now that discovery is closed, nothing happens.
        Assert.assertEquals(0, discovery.getAll().size());
        Assert.assertEquals(0, discovery.getAll(ServiceType.CONFIG).size());
        assertFalse(discovery.getRandom(ServiceType.CONFIG).isPresent());

        final Service service = new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3");
        discovery.register(service);

        // Still nothing registered.
        Assert.assertEquals(0, discovery.getAll().size());
        Assert.assertEquals(0, discovery.getAll(ServiceType.CONFIG).size());
        assertFalse(discovery.getRandom(ServiceType.CONFIG).isPresent());

        discovery.unregister(service);

        // Nothing registered still.
        Assert.assertEquals(0, discovery.getAll().size());
        Assert.assertEquals(0, discovery.getAll(ServiceType.CONFIG).size());
        assertFalse(discovery.getRandom(ServiceType.CONFIG).isPresent());
    }

    @Test(expected = DiscoveryException.class)
    public void testConstructorException() throws Exception {
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        Mockito.when(curator.getConnectionStateListenable()).thenThrow(new RuntimeException("Fake"));
        new CuratorDiscoveryManager(config, curator);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCloseWithException() throws Exception {
        final ServiceDiscovery<String> serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        Mockito.doThrow(new IOException("Fake")).when(serviceDiscovery).close();
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        Mockito.when(discovery.getDiscovery()).thenReturn(serviceDiscovery);
        Mockito.doCallRealMethod().when(discovery).close();
        discovery.close();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = DiscoveryException.class)
    public void testRegisterWithException() throws Exception {
        final ServiceDiscovery<String> serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        Mockito.doThrow(new Exception("Fake")).when(serviceDiscovery).registerService(Mockito.any());
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        Mockito.when(discovery.getDiscovery()).thenReturn(serviceDiscovery);
        Mockito.doCallRealMethod().when(discovery).register(Mockito.any());
        discovery.register(new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3"));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = DiscoveryException.class)
    public void testUnregisterWithException() throws Exception {
        final ServiceDiscovery<String> serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        Mockito.doThrow(new Exception("Fake")).when(serviceDiscovery).unregisterService(Mockito.any());
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        Mockito.when(discovery.getDiscovery()).thenReturn(serviceDiscovery);
        Mockito.doCallRealMethod().when(discovery).unregister(Mockito.any());
        discovery.unregister(new Service(ServiceType.CONFIG, "host", 1234, false, "1.2.3"));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = DiscoveryException.class)
    public void testGetAllForServiceTypeWithException() throws Exception {
        final ServiceDiscovery<String> serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        Mockito.doThrow(new Exception("Fake")).when(serviceDiscovery).queryForInstances(Mockito.anyString());
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        Mockito.when(discovery.getDiscovery()).thenReturn(serviceDiscovery);
        Mockito.doCallRealMethod().when(discovery).getAll(Mockito.any());
        discovery.getAll(ServiceType.CONFIG);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = DiscoveryException.class)
    public void testGetRandomWithException() throws Exception {
        final ServiceDiscovery<String> serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        Mockito.doThrow(new Exception("Fake")).when(serviceDiscovery).queryForInstances(Mockito.anyString());
        final CuratorDiscoveryManager discovery = Mockito.mock(CuratorDiscoveryManager.class);
        Mockito.when(discovery.getDiscovery()).thenReturn(serviceDiscovery);
        Mockito.doCallRealMethod().when(discovery).getRandom(Mockito.any());
        discovery.getRandom(ServiceType.CONFIG);
    }
}

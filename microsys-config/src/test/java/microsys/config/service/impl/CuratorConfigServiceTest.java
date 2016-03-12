package microsys.config.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.config.service.ConfigServiceException;
import microsys.service.model.ServiceEnvironment;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Perform testing on the {@link CuratorConfigService} class.
 */
public class CuratorConfigServiceTest {
    @Test
    public void test() throws Exception {
        // Don't want to see too much logging.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CuratorConfigService.class)).setLevel(Level.OFF);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final TestingServer testingServer = new TestingServer();
        final CuratorFramework curator = CuratorFrameworkFactory.builder().namespace("namespace-test")
                .connectString(testingServer.getConnectString()).defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        curator.blockUntilConnected(5, TimeUnit.SECONDS);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getCuratorFramework()).thenReturn(curator);

        try {
            final CuratorConfigService svc = new CuratorConfigService(serviceEnvironment);

            final ConfigKeyValueCollection coll = svc.getAll().get();
            assertEquals(0, coll.size());

            assertFalse(svc.set(new ConfigKeyValue("key", "value")).get().isPresent());

            // Wait a little to allow the value to be stored.
            TimeUnit.MILLISECONDS.sleep(300);

            final ConfigKeyValueCollection afterSet = svc.getAll().get();
            assertEquals(1, afterSet.size());

            // Overwrite the previous value.
            final Optional<ConfigKeyValue> oldValue = svc.set(new ConfigKeyValue("key", "new-value")).get();
            assertTrue(oldValue.isPresent());
            assertEquals(new ConfigKeyValue("key", "value"), oldValue.get());

            // Wait a little to allow the value to be stored.
            TimeUnit.MILLISECONDS.sleep(300);

            final Optional<ConfigKeyValue> get = svc.get("key").get();
            assertTrue(get.isPresent());
            assertEquals(new ConfigKeyValue("key", "new-value"), get.get());

            final Optional<ConfigKeyValue> unset = svc.unset("key").get();
            assertTrue(unset.isPresent());
            assertEquals(new ConfigKeyValue("key", "new-value"), unset.get());

            final Optional<ConfigKeyValue> getMissing = svc.get("missing").get();
            assertFalse(getMissing.isPresent());

            final Optional<ConfigKeyValue> unsetMissing = svc.unset("missing").get();
            assertFalse(unsetMissing.isPresent());
        } finally {
            curator.close();
            testingServer.close();
        }
    }

    @Test
    public void testExistingData() throws Exception {
        // Don't want to see too much logging.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CuratorConfigService.class)).setLevel(Level.OFF);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final TestingServer testingServer = new TestingServer();
        final CuratorFramework curator = CuratorFrameworkFactory.builder().namespace("namespace-test")
                .connectString(testingServer.getConnectString()).defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        curator.blockUntilConnected(5, TimeUnit.SECONDS);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getCuratorFramework()).thenReturn(curator);

        try {
            curator.create().forPath("/dynamic-config");
            curator.create().forPath("/dynamic-config/key", "value".getBytes(StandardCharsets.UTF_8));

            final CuratorConfigService svc = new CuratorConfigService(serviceEnvironment);

            // Wait a little to allow the value to be stored.
            TimeUnit.MILLISECONDS.sleep(300);

            final Optional<ConfigKeyValue> get = svc.get("key").get();
            assertTrue(get.isPresent());
            assertEquals(new ConfigKeyValue("key", "value"), get.get());
        } finally {
            curator.close();
            testingServer.close();
        }
    }

    @Test(expected = ExecutionException.class)
    public void testSetException() throws Exception {
        // Don't want to see too much logging.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CuratorConfigService.class)).setLevel(Level.OFF);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final TestingServer testingServer = new TestingServer();
        final CuratorFramework curator = CuratorFrameworkFactory.builder().namespace("namespace-set")
                .connectString(testingServer.getConnectString()).defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        curator.blockUntilConnected(5, TimeUnit.SECONDS);

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getCuratorFramework()).thenReturn(curator);

        try {
            final CuratorConfigService svc = new CuratorConfigService(serviceEnvironment);

            final ConfigKeyValue kv = Mockito.mock(ConfigKeyValue.class);
            Mockito.when(kv.getKey()).thenReturn("key");
            Mockito.when(kv.getValue()).thenThrow(new RuntimeException("Fake"));

            svc.set(kv).get();
        } finally {
            curator.close();
            testingServer.close();
        }
    }

    @Test(expected = ExecutionException.class)
    public void testUnsetException() throws Exception {
        // Don't want to see too much logging.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CuratorConfigService.class)).setLevel(Level.OFF);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final TestingServer testingServer = new TestingServer();

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getCuratorFramework()).thenThrow(new RuntimeException("Fake"));

        try {
            final TreeCache treeCache = Mockito.mock(TreeCache.class);
            Mockito.when(treeCache.getCurrentData(Mockito.anyString())).thenReturn(Mockito.mock(ChildData.class));

            final CuratorConfigService svc = Mockito.mock(CuratorConfigService.class);
            Mockito.when(svc.getServiceEnvironment()).thenReturn(serviceEnvironment);
            Mockito.when(svc.getTreeCache()).thenReturn(treeCache);
            Mockito.when(svc.unset(Mockito.anyString())).thenCallRealMethod();

            svc.unset("key").get();
        } finally {
            testingServer.close();
        }
    }

    @Test(expected = ConfigServiceException.class)
    public void testInitException() throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CuratorFramework curator = Mockito.mock(CuratorFramework.class);
        Mockito.when(curator.checkExists()).thenThrow(new RuntimeException("Fake"));

        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getExecutor()).thenReturn(executor);
        Mockito.when(serviceEnvironment.getCuratorFramework()).thenReturn(curator);

        new CuratorConfigService(serviceEnvironment);
    }
}

package microsys.security.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.common.config.CommonConfig;
import microsys.crypto.CryptoFactory;
import microsys.service.BaseService;
import microsys.service.discovery.DiscoveryManager;
import spark.webserver.JettySparkServer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Perform testing on the {@link Runner} class.
 */
public class RunnerTest {
    @Test
    public void test() throws Exception {
        // Don't want to see too much logging output.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);

        try (final TestingServer zookeeper = new TestingServer()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zookeeper.getConnectString()));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());

            new Runner(config, new CountDownLatch(1)).stop();
        }
    }

    @Test
    public void testWithParams() throws Exception {
        // Don't want to see too much logging output.
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JettySparkServer.class)).setLevel(Level.OFF);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(BaseService.class)).setLevel(Level.OFF);

        try (final TestingServer zookeeper = new TestingServer()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(CommonConfig.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zookeeper.getConnectString()));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
            final ExecutorService executor = Executors.newFixedThreadPool(3);
            final CuratorFramework curator = CuratorFrameworkFactory.builder().namespace("namespace")
                    .connectString(zookeeper.getConnectString()).defaultData(new byte[0])
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
            curator.start();
            final DiscoveryManager discovery = new DiscoveryManager(config, curator);
            final CryptoFactory crypto = new CryptoFactory(config);

            new Runner(config, executor, curator, discovery, crypto).stop();
        }
    }
}

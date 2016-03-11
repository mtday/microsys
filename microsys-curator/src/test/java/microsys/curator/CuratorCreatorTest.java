package microsys.curator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.config.ConfigKeys;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.crypto.impl.DefaultCryptoFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing on the {@link CuratorCreator} class.
 */
public class CuratorCreatorTest {
    @Test
    public void testConstructor() {
        // Just for 100% coverage.
        new CuratorCreator();
    }

    @Test
    public void testInsecure() throws Exception {
        final TestingServer zk = new TestingServer();

        CuratorFramework curator = null;
        try {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(ConfigKeys.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zk.getConnectString()));
            map.put(ConfigKeys.ZOOKEEPER_AUTH_ENABLED.getKey(), ConfigValueFactory.fromAnyRef(false));
            map.put(ConfigKeys.SYSTEM_NAME.getKey(), ConfigValueFactory.fromAnyRef("system"));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.systemProperties());
            final CryptoFactory crypto = new DefaultCryptoFactory(config);

            curator = CuratorCreator.create(config, crypto);
        } finally {
            if (curator != null) {
                curator.close();
            }
            zk.close();
        }
    }

    @Test
    public void testSecure() throws Exception {
        final TestingServer zk = new TestingServer();

        CuratorFramework curator = null;
        try {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(ConfigKeys.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zk.getConnectString()));
            map.put(ConfigKeys.ZOOKEEPER_AUTH_ENABLED.getKey(), ConfigValueFactory.fromAnyRef(true));
            map.put(ConfigKeys.ZOOKEEPER_AUTH_USER.getKey(), ConfigValueFactory.fromAnyRef("user"));
            map.put(ConfigKeys.ZOOKEEPER_AUTH_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("pass"));
            map.put(ConfigKeys.SYSTEM_NAME.getKey(), ConfigValueFactory.fromAnyRef("system"));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.systemProperties());
            final CryptoFactory crypto = new DefaultCryptoFactory(config);

            curator = CuratorCreator.create(config, crypto);
        } finally {
            if (curator != null) {
                curator.close();
            }
            zk.close();
        }
    }

    @Test(expected = CuratorException.class)
    public void testEncryptionException() throws Exception {
        final TestingServer zk = new TestingServer();

        try {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(ConfigKeys.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef(zk.getConnectString()));
            map.put(ConfigKeys.ZOOKEEPER_AUTH_ENABLED.getKey(), ConfigValueFactory.fromAnyRef(true));
            map.put(ConfigKeys.ZOOKEEPER_AUTH_USER.getKey(), ConfigValueFactory.fromAnyRef("user"));
            map.put(ConfigKeys.ZOOKEEPER_AUTH_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("pass"));
            map.put(ConfigKeys.SYSTEM_NAME.getKey(), ConfigValueFactory.fromAnyRef("system"));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.systemProperties());
            final CryptoFactory crypto = Mockito.mock(CryptoFactory.class);
            Mockito.when(crypto.getDecryptedConfig(Mockito.anyString())).thenThrow(new EncryptionException("Fake"));

            CuratorCreator.create(config, crypto);
        } finally {
            zk.close();
        }
    }

    @Test(expected = CuratorException.class)
    public void testConnectionException() throws Exception {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.ZOOKEEPER_HOSTS.getKey(), ConfigValueFactory.fromAnyRef("localhost:1"));
        map.put(ConfigKeys.ZOOKEEPER_AUTH_ENABLED.getKey(), ConfigValueFactory.fromAnyRef(true));
        map.put(ConfigKeys.ZOOKEEPER_AUTH_USER.getKey(), ConfigValueFactory.fromAnyRef("user"));
        map.put(ConfigKeys.ZOOKEEPER_AUTH_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("pass"));
        map.put(ConfigKeys.SYSTEM_NAME.getKey(), ConfigValueFactory.fromAnyRef("system"));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.systemProperties());
        final CryptoFactory crypto = new DefaultCryptoFactory(config);

        CuratorCreator.create(config, crypto);
    }
}

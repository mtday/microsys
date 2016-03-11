package microsys.curator;

import com.typesafe.config.Config;

import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import microsys.common.config.ConfigKeys;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * The base class for a micro service in this system.
 */
public class CuratorCreator {
    /**
     * @param config the static system configuration information
     * @param cryptoFactory the {@link CryptoFactory} used to decrypt zookeeper configuration properties
     * @return the created {@link CuratorFramework}
     *
     * @throws CuratorException if there is a problem creating the {@link CuratorFramework} or communicating with
     * zookeeper
     */
    @Nonnull
    public static CuratorFramework create(@Nonnull final Config config, @Nonnull final CryptoFactory cryptoFactory)
            throws CuratorException {
        Objects.requireNonNull(config);
        Objects.requireNonNull(cryptoFactory);

        final String zookeepers = config.getString(ConfigKeys.ZOOKEEPER_HOSTS.getKey());
        final boolean secure = config.getBoolean(ConfigKeys.ZOOKEEPER_AUTH_ENABLED.getKey());
        final String namespace = config.getString(ConfigKeys.SYSTEM_NAME.getKey());
        final CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.connectString(zookeepers);
        builder.namespace(namespace);
        builder.retryPolicy(new ExponentialBackoffRetry(1000, 3));
        builder.defaultData(new byte[0]);
        if (secure) {
            try {
                final String user = config.getString(ConfigKeys.ZOOKEEPER_AUTH_USER.getKey());
                final String pass = cryptoFactory.getDecryptedConfig(ConfigKeys.ZOOKEEPER_AUTH_PASSWORD.getKey());
                final byte[] authData = String.format("%s:%s", user, pass).getBytes(StandardCharsets.UTF_8);
                final AuthInfo authInfo = new AuthInfo("digest", authData);
                builder.authorization(Collections.singletonList(authInfo));
                builder.aclProvider(new CuratorACLProvider());
            } catch (final EncryptionException encryptionException) {
                throw new CuratorException("Failed to decrypt zookeeper password", encryptionException);
            }
        }
        final CuratorFramework curator = builder.build();
        curator.start();
        try {
            if (!curator.blockUntilConnected(2, TimeUnit.SECONDS)) {
                throw new CuratorException("Failed to connect to zookeeper");
            }
        } catch (final InterruptedException interrupted) {
            throw new CuratorException("Interrupted while waiting for curator to connect to zookeeper");
        }
        return curator;
    }
}

package microsys.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static microsys.common.config.CommonConfig.SHARED_SECRET_VARIABLE;
import static microsys.common.config.CommonConfig.SSL_ENABLED;
import static microsys.common.config.CommonConfig.SSL_KEYSTORE_FILE;
import static microsys.common.config.CommonConfig.SSL_KEYSTORE_PASSWORD;
import static microsys.common.config.CommonConfig.SSL_KEYSTORE_TYPE;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Perform testing on the {@link CryptoFactory} class.
 */
public class CryptoFactoryTest {
    @Test
    public void testPBEFromSystemEnvironment() throws EncryptionException {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(SHARED_SECRET_VARIABLE.getKey(), ConfigValueFactory.fromAnyRef("USER"));
        final Config config = ConfigFactory.parseMap(map);
        final CryptoFactory crypto = new CryptoFactory(config);

        final PasswordBasedEncryption pbe = crypto.getPasswordBasedEncryption();
        assertNotNull(pbe);

        final String original = "original data";
        final byte[] encrypted = pbe.encryptString(original, StandardCharsets.UTF_8);
        final String decrypted = pbe.decryptString(encrypted, StandardCharsets.UTF_8);
        assertEquals(original, decrypted);
    }

    @Test
    public void testPBEFromSystemProperties() throws EncryptionException {
        System.setProperty("SHARED_SECRET", "secret");
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(SHARED_SECRET_VARIABLE.getKey(), ConfigValueFactory.fromAnyRef("SHARED_SECRET"));
        final Config config = ConfigFactory.parseMap(map);
        final CryptoFactory crypto = new CryptoFactory(config);

        final PasswordBasedEncryption pbe = crypto.getPasswordBasedEncryption();
        assertNotNull(pbe);

        final String original = "original data";
        final byte[] encrypted = pbe.encryptString(original, StandardCharsets.UTF_8);
        final String decrypted = pbe.decryptString(encrypted, StandardCharsets.UTF_8);
        assertEquals(original, decrypted);
    }

    @Test(expected = EncryptionException.class)
    public void testPBENoSharedSecret() throws EncryptionException {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(SHARED_SECRET_VARIABLE.getKey(), ConfigValueFactory.fromAnyRef("DOES_NOT_EXIST"));
        final Config config = ConfigFactory.parseMap(map);
        final CryptoFactory crypto = new CryptoFactory(config);

        crypto.getPasswordBasedEncryption();
    }

    @Test(expected = EncryptionException.class)
    public void testSKEWithSslDisabled() throws EncryptionException {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("false"));
        final Config config = ConfigFactory.parseMap(map);
        final CryptoFactory crypto = new CryptoFactory(config);

        crypto.getSymmetricKeyEncryption();
    }

    @Test
    public void testSKEValidParams() throws EncryptionException {
        final Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource("keystore.jks"));
        if (url.isPresent()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(url.get().getFile()));
            map.put(SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
            final Config config = ConfigFactory.parseMap(map);
            final CryptoFactory crypto = new CryptoFactory(config);

            final SymmetricKeyEncryption ske = crypto.getSymmetricKeyEncryption();
            assertNotNull(ske);
        }
    }

    @Test(expected = EncryptionException.class)
    public void testSKEMissingKeyStoreFile() throws EncryptionException {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
        map.put(SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef("/tmp/missing-file.jks"));
        map.put(SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
        map.put(SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        final Config config = ConfigFactory.parseMap(map);
        final CryptoFactory crypto = new CryptoFactory(config);

        final SymmetricKeyEncryption ske = crypto.getSymmetricKeyEncryption();
        assertNotNull(ske);
    }

    @Test(expected = EncryptionException.class)
    public void testSKEInvalidKeyStoreFile() throws EncryptionException {
        final Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource("localhost.crt"));
        if (url.isPresent()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(url.get().getFile()));
            map.put(SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
            final Config config = ConfigFactory.parseMap(map);
            final CryptoFactory crypto = new CryptoFactory(config);

            final SymmetricKeyEncryption ske = crypto.getSymmetricKeyEncryption();
            assertNotNull(ske);
        }
    }

    @Test(expected = EncryptionException.class)
    public void testSKEMultipleKeyStoreFile() throws EncryptionException {
        final Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource("multiple.jks"));
        if (url.isPresent()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(url.get().getFile()));
            map.put(SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
            final Config config = ConfigFactory.parseMap(map);
            final CryptoFactory crypto = new CryptoFactory(config);

            final SymmetricKeyEncryption ske = crypto.getSymmetricKeyEncryption();
            assertNotNull(ske);
        }
    }

    @Test(expected = EncryptionException.class)
    public void testSKEImportKeyStoreFile() throws EncryptionException {
        final Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource("certificate.jks"));
        if (url.isPresent()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(url.get().getFile()));
            map.put(SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
            final Config config = ConfigFactory.parseMap(map);
            final CryptoFactory crypto = new CryptoFactory(config);

            final SymmetricKeyEncryption ske = crypto.getSymmetricKeyEncryption();
            assertNotNull(ske);
        }
    }
}

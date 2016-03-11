package microsys.crypto.impl;

import com.typesafe.config.Config;

import org.apache.commons.lang3.StringUtils;

import microsys.common.config.ConfigKeys;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.crypto.PasswordBasedEncryption;
import microsys.crypto.SymmetricKeyEncryption;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Provides access to the cryptography implementations used throughout this system.
 */
public class DefaultCryptoFactory implements CryptoFactory {
    @Nonnull
    private final Config config;

    /**
     * @param config the static system configuration
     */
    public DefaultCryptoFactory(@Nonnull final Config config) {
        this.config = Objects.requireNonNull(config);
    }

    /**
     * @return the static system configuration information
     */
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the shared secret defined for the system
     * @throws EncryptionException if there is a problem retrieving the shared secret value
     */
    @Nonnull
    protected String getSharedSecret() throws EncryptionException {
        final String sharedSecretVar = getConfig().getString(ConfigKeys.SHARED_SECRET_VARIABLE.getKey());
        if (getConfig().hasPath(sharedSecretVar)) {
            return getConfig().getString(sharedSecretVar);
        }
        throw new EncryptionException("Failed to retrieve shared secret from variable " + sharedSecretVar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public PasswordBasedEncryption getPasswordBasedEncryption() throws EncryptionException {
        return new AESPasswordBasedEncryption(getSharedSecret().toCharArray());
    }

    /**
     * @param file the file from which the key store should be loaded
     * @param type the type of key store contained in the file
     * @param pass the key store password
     * @return the {@link KeyStore} from retrieved from the provided file
     * @throws EncryptionException if there is a problem retrieving the key store
     */
    @Nonnull
    protected KeyStore getStore(@Nonnull final String file, @Nonnull final String type, @Nonnull final char[] pass)
            throws EncryptionException {
        try (final FileInputStream fis = new FileInputStream(Objects.requireNonNull(file))) {
            final KeyStore keyStore = KeyStore.getInstance(Objects.requireNonNull(type));
            keyStore.load(fis, Objects.requireNonNull(pass));
            return keyStore;
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to load key store from file " + file + " (with type " + type + ")",
                    exception);
        }
    }

    /**
     * @return the {@link KeyStore} from which the system public and private keys will be retrieved
     * @throws EncryptionException if there is a problem retrieving the key store
     */
    @Nonnull
    protected KeyStore getKeyStore() throws EncryptionException {
        final String file = getConfig().getString(ConfigKeys.SSL_KEYSTORE_FILE.getKey());
        final String type = getConfig().getString(ConfigKeys.SSL_KEYSTORE_TYPE.getKey());
        final char[] pass = getDecryptedConfig(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey()).toCharArray();
        return getStore(file, type, pass);
    }

    /**
     * @return the {@link KeyStore} from which the system trusted certificates will be retrieved
     * @throws EncryptionException if there is a problem retrieving the trust store
     */
    @Nonnull
    protected KeyStore getTrustStore() throws EncryptionException {
        final String file = getConfig().getString(ConfigKeys.SSL_TRUSTSTORE_FILE.getKey());
        final String type = getConfig().getString(ConfigKeys.SSL_TRUSTSTORE_TYPE.getKey());
        final char[] pass = getDecryptedConfig(ConfigKeys.SSL_TRUSTSTORE_PASSWORD.getKey()).toCharArray();
        return getStore(file, type, pass);
    }

    /**
     * @return a {@link KeyPair} representing the system public and private keys
     * @throws EncryptionException if there is a problem retrieving the key pair
     */
    @Nonnull
    protected KeyPair getSymmetricKeyPair() throws EncryptionException {
        try {
            final KeyStore keyStore = getKeyStore();
            if (keyStore.size() > 1) {
                throw new Exception("Key store files with more than one entry are not supported");
            }
            final String alias = keyStore.aliases().nextElement();
            if (keyStore.isKeyEntry(alias)) {
                final char[] pass = getDecryptedConfig(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey()).toCharArray();
                final Key key = keyStore.getKey(alias, pass);
                return new KeyPair(keyStore.getCertificate(alias).getPublicKey(), (PrivateKey) key);
            } else {
                throw new Exception("Key store alias " + alias + " is not of type key");
            }
        } catch (final EncryptionException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to retrieve symmetric keys from key store", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SymmetricKeyEncryption getSymmetricKeyEncryption() throws EncryptionException {
        return new AESSymmetricKeyEncryption(getSymmetricKeyPair());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SSLContext getSSLContext() throws EncryptionException {
        try {
            if (!getConfig().getBoolean(ConfigKeys.SSL_ENABLED.getKey())) {
                return SSLContext.getDefault();
            }

            final KeyStore keyStore = getKeyStore();
            final KeyStore trustStore = getTrustStore();

            final char[] pass = getDecryptedConfig(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey()).toCharArray();
            final KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, pass);

            final TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
                    new SecureRandom());
            return sslContext;
        } catch (final EncryptionException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to create SSLContext", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getDecryptedConfig(@Nonnull final String key) throws EncryptionException {
        final String value = Objects.requireNonNull(getConfig()).getString(Objects.requireNonNull(key));
        if (StringUtils.startsWith(value, "PBE{") && StringUtils.endsWith(value, "}")) {
            return getPasswordBasedEncryption()
                    .decryptString(value.substring(4, value.length() - 1), StandardCharsets.UTF_8);
        } else if (StringUtils.startsWith(value, "SKE{") && StringUtils.endsWith(value, "}")) {
            return getSymmetricKeyEncryption()
                    .decryptString(value.substring(4, value.length() - 1), StandardCharsets.UTF_8);
        } else {
            return value;
        }
    }
}

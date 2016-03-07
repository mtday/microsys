package microsys.crypto;

import com.typesafe.config.Config;

import microsys.common.config.CommonConfig;
import microsys.crypto.impl.AESPasswordBasedEncryption;
import microsys.crypto.impl.AESSymmetricKeyEncryption;

import java.io.FileInputStream;
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
public class CryptoFactory {
    @Nonnull
    private final Config config;

    /**
     * @param config the static system configuration
     */
    public CryptoFactory(@Nonnull final Config config) {
        this.config = Objects.requireNonNull(config);
    }

    /**
     * @return the static system configuration information
     */
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the shared secret defined for the system, if available
     * @throws EncryptionException if there is a problem retrieving the shared secret value
     */
    @Nonnull
    protected String getSharedSecret() throws EncryptionException {
        final String sharedSecretVar = getConfig().getString(CommonConfig.SHARED_SECRET_VARIABLE.getKey());
        final String sharedSecretFromEnv = System.getenv(sharedSecretVar);
        if (sharedSecretFromEnv == null) {
            final String sharedSecretFromProps = System.getProperty(sharedSecretVar);
            if (sharedSecretFromProps == null) {
                throw new EncryptionException("Failed to retrieve shared secret from variable " + sharedSecretVar);
            }
            return sharedSecretFromProps;
        }
        return sharedSecretFromEnv;
    }

    /**
     * @return the {@link PasswordBasedEncryption} to use when encrypting and decrypting system data
     */
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
        final String file = getConfig().getString(CommonConfig.SSL_KEYSTORE_FILE.getKey());
        final String type = getConfig().getString(CommonConfig.SSL_KEYSTORE_TYPE.getKey());
        final char[] pass = getConfig().getString(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey()).toCharArray();
        return getStore(file, type, pass);
    }

    /**
     * @return the {@link KeyStore} from which the system trusted certificates will be retrieved
     * @throws EncryptionException if there is a problem retrieving the trust store
     */
    @Nonnull
    protected KeyStore getTrustStore() throws EncryptionException {
        final String file = getConfig().getString(CommonConfig.SSL_TRUSTSTORE_FILE.getKey());
        final String type = getConfig().getString(CommonConfig.SSL_TRUSTSTORE_TYPE.getKey());
        final char[] pass = getConfig().getString(CommonConfig.SSL_TRUSTSTORE_PASSWORD.getKey()).toCharArray();
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
                final char[] pass = getConfig().getString(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey()).toCharArray();
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
     * @return the {@link SymmetricKeyEncryption} to use when encrypting, decrypting, and signing system data
     * @throws EncryptionException if there is a problem creating the symmetric-key encryption implementation
     */
    @Nonnull
    public SymmetricKeyEncryption getSymmetricKeyEncryption() throws EncryptionException {
        return new AESSymmetricKeyEncryption(getSymmetricKeyPair());
    }

    /**
     * @return a configured {@link SSLContext} based on the static SSL configuration
     * @throws EncryptionException if there is a problem creating or initializing the {@link SSLContext}
     */
    @Nonnull
    public SSLContext getSSLContext() throws EncryptionException {
        try {
            if (!getConfig().getBoolean(CommonConfig.SSL_ENABLED.getKey())) {
                return SSLContext.getDefault();
            }

            final KeyStore keyStore = getKeyStore();
            final KeyStore trustStore = getTrustStore();

            final char[] keyPass = getConfig().getString(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey()).toCharArray();
            final KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyPass);

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
}

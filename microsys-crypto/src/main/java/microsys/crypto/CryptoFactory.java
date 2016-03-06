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
import java.util.Objects;

import javax.annotation.Nonnull;

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
     * @return the {@link KeyStore} from which the system public and private keys will be retrieved
     * @throws EncryptionException if there is a problem retrieving the key store
     */
    @Nonnull
    protected KeyStore getKeyStore() throws EncryptionException {
        final boolean sslEnabled = getConfig().getBoolean(CommonConfig.SSL_ENABLED.getKey());
        if (!sslEnabled) {
            throw new EncryptionException(
                    "SSL is disabled, so unable to retrieve key store for symmetric key encryption");
        }

        final String file = getConfig().getString(CommonConfig.SSL_KEYSTORE_FILE.getKey());
        final String type = getConfig().getString(CommonConfig.SSL_KEYSTORE_TYPE.getKey());
        final char[] pass = getConfig().getString(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey()).toCharArray();

        try (final FileInputStream fis = new FileInputStream(file)) {
            final KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(fis, pass);
            return keyStore;
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to load key store from file " + file + " (with type " + type + ")",
                    exception);
        }
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
     */
    @Nonnull
    public SymmetricKeyEncryption getSymmetricKeyEncryption() throws EncryptionException {
        return new AESSymmetricKeyEncryption(getSymmetricKeyPair());
    }
}

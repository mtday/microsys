package microsys.crypto;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

/**
 * Defines the interface provided by system crypto providers.
 */
public interface CryptoFactory {
    /**
     * @return the {@link PasswordBasedEncryption} to use when encrypting and decrypting system data
     * @throws EncryptionException if there is a problem creating the password-based encryption implementation
     */
    @Nonnull
    PasswordBasedEncryption getPasswordBasedEncryption() throws EncryptionException;

    /**
     * @return the {@link SymmetricKeyEncryption} to use when encrypting, decrypting, and signing system data
     * @throws EncryptionException if there is a problem creating the symmetric-key encryption implementation
     */
    @Nonnull
    SymmetricKeyEncryption getSymmetricKeyEncryption() throws EncryptionException;

    /**
     * @return a configured {@link SSLContext} based on the static SSL configuration
     * @throws EncryptionException if there is a problem creating or initializing the {@link SSLContext}
     */
    @Nonnull
    public SSLContext getSSLContext() throws EncryptionException;

    /**
     * @param key the key within the static system configuration for which the value should be retrieved
     * @return the requested configuration value, decrypting it if necessary
     * @throws EncryptionException if there is a problem performing the decryption
     */
    @Nonnull
    String getDecryptedConfig(@Nonnull String key) throws EncryptionException;
}

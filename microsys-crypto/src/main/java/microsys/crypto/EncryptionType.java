package microsys.crypto;

/**
 * Defines the available encryption types.
 */
public enum EncryptionType {
    /**
     * Encryption based on a shared password.
     */
    PASSWORD_BASED,

    /**
     * Encryption based on the system symmetric public and private keys.
     */
    SYMMETRIC_KEY,
}

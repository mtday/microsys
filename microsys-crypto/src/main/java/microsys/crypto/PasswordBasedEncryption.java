package microsys.crypto;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Responsible for performing password-based encryption.
 */
public interface PasswordBasedEncryption {
    /**
     * Encrypt data from the provided input stream and write to the provided output stream.
     *
     * @param input  an arbitrary byte stream to encrypt
     * @param output the stream to which encrypted data will be written
     * @throws EncryptionException if there is a problem performing the encryption
     */
    void encrypt(final InputStream input, final OutputStream output) throws EncryptionException;

    /**
     * Encrypt the provided data byte array and return the encrypted data.
     *
     * @param data the data to be encrypted
     * @return the encrypted data
     * @throws EncryptionException if there is a problem performing the encryption
     */
    byte[] encrypt(byte[] data) throws EncryptionException;

    /**
     * Encrypt the provided data string and return the encrypted data.
     *
     * @param data the data to be encrypted as a string
     * @param charset the {@link Charset} to use when retrieving bytes from the string
     * @return the encrypted data
     * @throws EncryptionException if there is a problem performing the encryption
     */
    byte[] encryptString(String data, Charset charset) throws EncryptionException;

    /**
     * Decrypt data from the provided input stream and write to the provided output stream.
     *
     * @param input  an arbitrary byte stream to decrypt
     * @param output the stream to which decrypted data will be written
     * @throws EncryptionException if there is a problem performing the decryption
     */
    void decrypt(final InputStream input, final OutputStream output) throws EncryptionException;

    /**
     * Decrypt the provided data byte array and return the unencrypted data.
     *
     * @param data the data to be decrypted
     * @return the decrypted data
     * @throws EncryptionException if there is a problem performing the decryption
     */
    byte[] decrypt(byte[] data) throws EncryptionException;

    /**
     * Decrypt the provided data string and return the unencrypted data.
     *
     * @param data the data to be decrypted
     * @param charset the {@link Charset} to use when recreating the string value
     * @return the decrypted data as a string
     * @throws EncryptionException if there is a problem performing the decryption
     */
    String decryptString(byte[] data, Charset charset) throws EncryptionException;
}

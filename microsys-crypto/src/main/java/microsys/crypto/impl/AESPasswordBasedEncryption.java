package microsys.crypto.impl;

import microsys.crypto.EncryptionException;
import microsys.crypto.PasswordBasedEncryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides an implementation of the {@link PasswordBasedEncryption} interface, and is responsible for performing
 * password-based encryption of system data.
 */
public class AESPasswordBasedEncryption implements PasswordBasedEncryption {
    private final static String ALGORITHM = "AES";

    // AES specification - changing will break existing encrypted streams!
    private final static String CIPHER_SPEC = "AES/CBC/PKCS5Padding";

    // Key derivation specification - changing will break existing streams!
    private final static String KEYGEN_SPEC = "PBKDF2WithHmacSHA1";
    private final static int SALT_LENGTH = 16; // in bytes
    private final static int ITERATIONS = 32768;

    // Process input/output streams in chunks - arbitrary
    private final static int BUFFER_SIZE = 1024;

    @Nonnull
    private final char[] password;

    /**
     * @param password the password to use when encrypting and decrypting data
     */
    public AESPasswordBasedEncryption(@Nonnull final char[] password) {
        this.password = Objects.requireNonNull(password);
    }

    /**
     * @return a new pseudorandom salt of the specified length
     */
    @Nonnull
    protected byte[] generateSalt(final int length) {
        final Random random = new SecureRandom();
        final byte[] salt = new byte[length];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Generate an AES encryption key from a password and salt.
     *
     * @param keyLength the length of the key to generate
     * @param salt the salt from which to derive the keys
     * @return the {@link SecretKey} used to perform the encryption
     */
    @Nonnull
    protected SecretKey keygen(final int keyLength, @Nonnull final byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEYGEN_SPEC);
        final KeySpec keySpec = new PBEKeySpec(this.password, salt, ITERATIONS, keyLength);
        final SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public void encrypt(@Nonnull final InputStream input, @Nonnull final OutputStream output)
            throws EncryptionException {
        try {
            // Generate salt and derive keys for authentication and encryption
            final int keyLength = 128; // The unlimited-strength jce not required for this.
            final byte[] salt = generateSalt(SALT_LENGTH);
            final SecretKey key = keygen(keyLength, salt);

            // Initialize AES encryption
            final Cipher cipher = Cipher.getInstance(CIPHER_SPEC);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Get initialization vector
            final byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();

            // Write authentication and AES initialization data
            output.write(keyLength / 8);
            output.write(salt);
            output.write(iv);

            // Read data from input into buffer, encrypt and write to output
            final byte[] buffer = new byte[BUFFER_SIZE];
            int numRead;
            while ((numRead = input.read(buffer)) > 0) {
                output.write(cipher.update(buffer, 0, numRead));
            }
            output.write(cipher.doFinal());
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to encrypt data", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public byte[] encrypt(@Nonnull final byte[] data) throws EncryptionException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        encrypt(new ByteArrayInputStream(data), output);
        return output.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public byte[] encryptString(@Nonnull final String data, @Nonnull final Charset charset) throws EncryptionException {
        return encrypt(Objects.requireNonNull(data).getBytes(charset));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public void decrypt(@Nonnull final InputStream input, @Nonnull final OutputStream output)
            throws EncryptionException {
        try {
            final int keyLength = input.read() * 8;

            // Read the salt value
            final byte[] salt = new byte[SALT_LENGTH];
            int saltRead = 0;
            while (saltRead < SALT_LENGTH) {
                saltRead += input.read(salt, saltRead, SALT_LENGTH - saltRead);
            }

            // Read the initialization vector value
            final byte[] iv = new byte[16]; // 16-byte initialization vector regardless of key size
            int ivRead = 0;
            while (ivRead < 16) {
                ivRead += input.read(iv, ivRead, 16 - ivRead);
            }

            // Initialize AES decryption
            final Cipher cipher = Cipher.getInstance(CIPHER_SPEC);
            cipher.init(Cipher.DECRYPT_MODE, keygen(keyLength, salt), new IvParameterSpec(iv));

            // Read data from input into buffer, decrypt and write to output
            final byte[] buffer = new byte[BUFFER_SIZE];
            int numRead;
            while ((numRead = input.read(buffer)) > 0) {
                output.write(cipher.update(buffer, 0, numRead));
            }
            output.write(cipher.doFinal());
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to decrypt data", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public byte[] decrypt(@Nonnull final byte[] data) throws EncryptionException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        decrypt(new ByteArrayInputStream(data), output);
        return output.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String decryptString(@Nonnull final byte[] data, @Nonnull final Charset charset) throws EncryptionException {
        return new String(decrypt(data), charset);
    }
}

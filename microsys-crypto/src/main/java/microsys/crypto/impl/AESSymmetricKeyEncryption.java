package microsys.crypto.impl;

import microsys.crypto.EncryptionException;
import microsys.crypto.SymmetricKeyEncryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides an implementation of the {@link SymmetricKeyEncryption} interface, using the the AES algorithm to perform
 * the encryption and decryption of data (with the key encrypted using the symmetric-key algorithm), along with using
 * the provided {@link KeyPair} to perform symmetric-key encryption, decryption, and signing operations.
 */
public class AESSymmetricKeyEncryption implements SymmetricKeyEncryption {
    private final static String ALGORITHM = "AES";

    // Process input/output streams in chunks - arbitrary
    private final static int BUFFER_SIZE = 1024;

    @Nonnull
    private final KeyPair keyPair;

    /**
     * @param keyPair the {@link KeyPair} containing the public and private symmetric keys
     */
    public AESSymmetricKeyEncryption(@Nonnull final KeyPair keyPair) throws EncryptionException {
        this.keyPair = Objects.requireNonNull(keyPair);
    }

    /**
     * @return the {@link KeyPair} containing the public and private symmetric keys
     */
    protected KeyPair getKeyPair() {
        return this.keyPair;
    }

    protected final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * @param bytes the byte array to be converted into a string of hex characters
     * @return the string of hex characters
     */
    protected static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * @param hex the hex string to convert into a byte array
     * @return the byte array represented by the hex string
     */
    protected static byte[] hexToBytes(final String hex) {
        final int len = hex.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * @param keyLength the length of the key to generate
     * @return the {@link SecretKey} used to do the encryption and decryption of system data
     */
    protected SecretKey createSecretKey(final int keyLength) throws EncryptionException {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(keyLength);
            return keyGenerator.generateKey();
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to generate secret key for encryption", exception);
        }
    }

    /**
     * @param secretKey the {@link SecretKey} to encrypt
     * @return the bytes representing the encrypted secret key
     */
    @Nonnull
    protected byte[] getEncryptedSecretKey(@Nonnull final SecretKey secretKey) throws Exception {
        Objects.requireNonNull(secretKey);
        final PrivateKey privateKey = getKeyPair().getPrivate();
        final Cipher symmetricCipher = Cipher.getInstance(privateKey.getAlgorithm());
        symmetricCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return symmetricCipher.doFinal(secretKey.getEncoded());
    }

    /**
     * @param encrypted the encrypted secret key
     * @return the {@link SecretKeySpec} decrypted from the provided secret key encrypted bytes
     */
    @Nonnull
    protected SecretKeySpec getDecryptedSecretKey(@Nonnull final byte[] encrypted) throws Exception {
        Objects.requireNonNull(encrypted);
        final PublicKey publicKey = getKeyPair().getPublic();
        final Cipher symmetricCipher = Cipher.getInstance(publicKey.getAlgorithm());
        symmetricCipher.init(Cipher.DECRYPT_MODE, publicKey);
        return new SecretKeySpec(symmetricCipher.doFinal(encrypted), ALGORITHM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public void encrypt(@Nonnull final InputStream input, @Nonnull final OutputStream output)
            throws EncryptionException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        try {
            final int keyLength = 128; // The unlimited-strength jce not required for this.
            final SecretKey secretKey = createSecretKey(keyLength);
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), ALGORITHM));

            // First, write the secret key into the output stream
            final byte[] encryptedSecretKey = getEncryptedSecretKey(secretKey);
            output.write(encryptedSecretKey.length / 8);
            output.write(encryptedSecretKey);

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
        encrypt(new ByteArrayInputStream(Objects.requireNonNull(data)), output);
        return output.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public byte[] encryptString(@Nonnull final String data, @Nonnull final Charset charset) throws EncryptionException {
        return encrypt(Objects.requireNonNull(data).getBytes(Objects.requireNonNull(charset)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public void decrypt(@Nonnull final InputStream input, @Nonnull final OutputStream output)
            throws EncryptionException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        try {
            // Read the encrypted key value
            final int encryptedKeyLength = input.read() * 8;
            final byte[] encryptedKey = new byte[encryptedKeyLength];
            int keyRead = 0;
            while (keyRead < encryptedKeyLength) {
                keyRead += input.read(encryptedKey, keyRead, encryptedKeyLength - keyRead);
            }

            final SecretKeySpec secretKey = getDecryptedSecretKey(encryptedKey);

            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

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
        decrypt(new ByteArrayInputStream(Objects.requireNonNull(data)), output);
        return output.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String decryptString(@Nonnull final byte[] data, @Nonnull final Charset charset) throws EncryptionException {
        return new String(decrypt(data), Objects.requireNonNull(charset));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public byte[] sign(@Nonnull final byte[] data) throws EncryptionException {
        Objects.requireNonNull(data);
        try {
            final PrivateKey privateKey = getKeyPair().getPrivate();
            final Signature signature = Signature.getInstance("SHA1with" + privateKey.getAlgorithm());
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to sign data", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String signString(@Nonnull final String data, @Nonnull final Charset charset) throws EncryptionException {
        return bytesToHex(sign(Objects.requireNonNull(data).getBytes(Objects.requireNonNull(charset))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public boolean verify(@Nonnull final byte[] data, @Nonnull final byte[] signatureData) throws EncryptionException {
        Objects.requireNonNull(data);
        try {
            final PublicKey publicKey = getKeyPair().getPublic();
            final Signature signature = Signature.getInstance("SHA1with" + publicKey.getAlgorithm());
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signatureData);
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to verify signature", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public boolean verifyString(
            @Nonnull final String data, @Nonnull final Charset charset, @Nonnull final String signatureData)
            throws EncryptionException {
        final byte[] dataBytes = Objects.requireNonNull(data).getBytes(Objects.requireNonNull(charset));
        return verify(dataBytes, hexToBytes(signatureData));
    }
}

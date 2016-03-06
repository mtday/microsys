package microsys.crypto.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.crypto.EncryptionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Perform testing on the {@link AESSymmetricKeyEncryption} class.
 */
public class AESSymmetricKeyEncryptionTest {
    protected KeyPair getKeyPair() throws EncryptionException {
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (final NoSuchAlgorithmException badAlgorithm) {
            throw new EncryptionException("Unrecognized algorithm", badAlgorithm);
        }
    }

    @Test
    public void testRoundTripStreamSame() throws EncryptionException, IOException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final byte[] original = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final ByteArrayInputStream originalInput = new ByteArrayInputStream(original);
        final ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();

        ske.encrypt(originalInput, encryptedOutput);
        encryptedOutput.close();

        final ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
        final ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();

        ske.decrypt(encryptedInput, decryptedOutput);
        decryptedOutput.close();

        final byte[] decrypted = decryptedOutput.toByteArray();
        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripStreamMultiple() throws EncryptionException, IOException {
        final KeyPair keyPair = getKeyPair();
        final AESSymmetricKeyEncryption ske1 = new AESSymmetricKeyEncryption(keyPair);
        final AESSymmetricKeyEncryption ske2 = new AESSymmetricKeyEncryption(keyPair);

        final byte[] original = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final ByteArrayInputStream originalInput = new ByteArrayInputStream(original);
        final ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();

        ske1.encrypt(originalInput, encryptedOutput);
        encryptedOutput.close();

        final ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
        final ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();

        ske2.decrypt(encryptedInput, decryptedOutput);
        decryptedOutput.close();

        final byte[] decrypted = decryptedOutput.toByteArray();
        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripStreamEmpty() throws EncryptionException, IOException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final byte[] original = new byte[0];
        final ByteArrayInputStream originalInput = new ByteArrayInputStream(original);
        final ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();

        ske.encrypt(originalInput, encryptedOutput);
        encryptedOutput.close();

        final ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
        final ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();

        ske.decrypt(encryptedInput, decryptedOutput);
        decryptedOutput.close();

        final byte[] decrypted = decryptedOutput.toByteArray();
        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripStreamBufferSize() throws EncryptionException, IOException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final byte[] original = new byte[1024];
        final ByteArrayInputStream originalInput = new ByteArrayInputStream(original);
        final ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();

        ske.encrypt(originalInput, encryptedOutput);
        encryptedOutput.close();

        final ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
        final ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();

        ske.decrypt(encryptedInput, decryptedOutput);
        decryptedOutput.close();

        final byte[] decrypted = decryptedOutput.toByteArray();
        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripByteArraySame() throws EncryptionException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final byte[] original = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final byte[] encrypted = ske.encrypt(original);
        final byte[] decrypted = ske.decrypt(encrypted);

        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripByteArrayMultiple() throws EncryptionException {
        final KeyPair keyPair = getKeyPair();
        final AESSymmetricKeyEncryption ske1 = new AESSymmetricKeyEncryption(keyPair);
        final AESSymmetricKeyEncryption ske2 = new AESSymmetricKeyEncryption(keyPair);

        final byte[] original = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final byte[] encrypted = ske1.encrypt(original);
        final byte[] decrypted = ske2.decrypt(encrypted);

        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripStringSame() throws EncryptionException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final String original = "original data";
        final byte[] encrypted = ske.encryptString(original, StandardCharsets.UTF_8);
        final String decrypted = ske.decryptString(encrypted, StandardCharsets.UTF_8);

        assertEquals(original, decrypted);
    }

    @Test
    public void testRoundTripStringMultiple() throws EncryptionException {
        final KeyPair keyPair = getKeyPair();
        final AESSymmetricKeyEncryption ske1 = new AESSymmetricKeyEncryption(keyPair);
        final AESSymmetricKeyEncryption ske2 = new AESSymmetricKeyEncryption(keyPair);

        final String original = "original data";
        final byte[] encrypted = ske1.encryptString(original, StandardCharsets.UTF_8);
        final String decrypted = ske2.decryptString(encrypted, StandardCharsets.UTF_8);

        assertEquals(original, decrypted);
    }

    @Test(expected = EncryptionException.class)
    public void testEncryptStreamThrowsException() throws EncryptionException, IOException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final ByteArrayInputStream input = Mockito.mock(ByteArrayInputStream.class);
        Mockito.when(input.read(Mockito.any())).thenThrow(new IOException("Failed"));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        ske.encrypt(input, output);
    }

    @Test(expected = EncryptionException.class)
    public void testDecryptStreamThrowsException() throws EncryptionException, IOException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final String original = "original data";
        final byte[] encrypted = ske.encryptString(original, StandardCharsets.UTF_8);
        final ByteArrayInputStream input = new ByteArrayInputStream(encrypted);
        final ByteArrayOutputStream output = Mockito.mock(ByteArrayOutputStream.class);
        Mockito.doThrow(new IOException("Failed")).when(output).write(Mockito.any());

        ske.decrypt(input, output);
    }

    @Test
    public void testSignVerifyByteArraySame() throws EncryptionException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final byte[] data = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final byte[] signature = ske.sign(data);
        final boolean verified = ske.verify(data, signature);

        assertTrue(verified);
    }

    @Test
    public void testSignVerifyByteArrayMultiple() throws EncryptionException {
        final KeyPair keyPair = getKeyPair();
        final AESSymmetricKeyEncryption ske1 = new AESSymmetricKeyEncryption(keyPair);
        final AESSymmetricKeyEncryption ske2 = new AESSymmetricKeyEncryption(keyPair);

        final byte[] data = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final byte[] signature = ske1.sign(data);
        final boolean verified = ske2.verify(data, signature);

        assertTrue(verified);
    }

    @Test
    public void testSignVerifyStringSame() throws EncryptionException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final String data = "original data";
        final String signature = ske.signString(data, StandardCharsets.UTF_8);
        final boolean verified = ske.verifyString(data, StandardCharsets.UTF_8, signature);

        assertTrue(verified);
    }

    @Test
    public void testSignVerifyStringMultiple() throws EncryptionException {
        final KeyPair keyPair = getKeyPair();
        final AESSymmetricKeyEncryption ske1 = new AESSymmetricKeyEncryption(keyPair);
        final AESSymmetricKeyEncryption ske2 = new AESSymmetricKeyEncryption(keyPair);

        final String data = "original data";
        final String signature = ske1.signString(data, StandardCharsets.UTF_8);
        final boolean verified = ske2.verifyString(data, StandardCharsets.UTF_8, signature);

        assertTrue(verified);
    }

    @Test
    public void testSignVerifyByteArrayEmpty() throws EncryptionException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final byte[] data = new byte[0];
        final byte[] signature = ske.sign(data);
        final boolean verified = ske.verify(data, signature);

        assertTrue(verified);
    }

    @Test
    public void testSignVerifyStringEmpty() throws EncryptionException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());

        final String signature = ske.signString("", StandardCharsets.UTF_8);
        final boolean verified = ske.verifyString("", StandardCharsets.UTF_8, signature);

        assertTrue(verified);
    }

    @Test(expected = EncryptionException.class)
    public void testSignException() throws EncryptionException {
        final PublicKey publicKey = Mockito.mock(PublicKey.class);
        final PrivateKey privateKey = Mockito.mock(PrivateKey.class);
        Mockito.when(privateKey.getAlgorithm()).thenThrow(new RuntimeException("Fake"));
        final KeyPair keyPair = new KeyPair(publicKey, privateKey);

        final AESSymmetricKeyEncryption ske = Mockito.mock(AESSymmetricKeyEncryption.class);
        Mockito.when(ske.getKeyPair()).thenReturn(keyPair);
        Mockito.when(ske.sign(Mockito.any())).thenCallRealMethod();

        ske.sign(new byte[0]);
    }

    @Test(expected = EncryptionException.class)
    public void testVerifyException() throws EncryptionException {
        final PublicKey publicKey = Mockito.mock(PublicKey.class);
        Mockito.when(publicKey.getAlgorithm()).thenThrow(new RuntimeException("Fake"));
        final PrivateKey privateKey = Mockito.mock(PrivateKey.class);
        final KeyPair keyPair = new KeyPair(publicKey, privateKey);

        final AESSymmetricKeyEncryption ske = Mockito.mock(AESSymmetricKeyEncryption.class);
        Mockito.when(ske.getKeyPair()).thenReturn(keyPair);
        Mockito.when(ske.verify(Mockito.any(), Mockito.any())).thenCallRealMethod();

        ske.verify(new byte[0], new byte[0]);
    }

    @Test(expected = EncryptionException.class)
    public void testCreateSecretKeyException() throws EncryptionException {
        final AESSymmetricKeyEncryption ske = new AESSymmetricKeyEncryption(getKeyPair());
        ske.createSecretKey(-1);
    }
}

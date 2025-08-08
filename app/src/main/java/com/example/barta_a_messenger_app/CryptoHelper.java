package com.example.barta_a_messenger_app;

import android.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS7Padding";
    private static final String FALLBACK_KEY = "H@rrY_p0tter_106"; // Keep as fallback for old messages

    public static String encrypt(String key, String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.encodeToString(encryptedBytes,Base64.DEFAULT);
    }

    public static String decrypt(String key, String encryptedData) throws Exception {
        byte[] encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT);

        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Encrypts data using the fallback key for compatibility with old messages
     * @param data The data to encrypt
     * @return Encrypted data
     * @throws Exception if encryption fails
     */
    public static String encryptWithFallbackKey(String data) throws Exception {
        return encrypt(FALLBACK_KEY, data);
    }

    /**
     * Decrypts data using the fallback key for compatibility with old messages
     * @param encryptedData The encrypted data to decrypt
     * @return Decrypted data
     * @throws Exception if decryption fails
     */
    public static String decryptWithFallbackKey(String encryptedData) throws Exception {
        return decrypt(FALLBACK_KEY, encryptedData);
    }

    /**
     * Gets the fallback encryption key
     * @return The fallback key
     */
    public static String getFallbackKey() {
        return FALLBACK_KEY;
    }
}

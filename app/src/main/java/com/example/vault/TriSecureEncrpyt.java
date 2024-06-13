package com.example.vault;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TriSecureEncrpyt {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;

    public static void main(String[] args) {
        try {
            String plaintext = "Hello, SecureMultiLevel!";
            KeyPair keyPair = generateRSAKeyPair();
            String encryptedData = encrypt(plaintext, keyPair.getPublic());
            System.out.println("Encrypted Data: " + encryptedData);
            String decryptedData = decrypt(encryptedData, keyPair.getPrivate());
            System.out.println("Decrypted Data: " + decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public static String encrypt(String plaintext, PublicKey publicKey) throws Exception {
        // Generate AES key and IV
        SecretKey aesKey = generateAESKey();
        byte[] iv = generateIV();

        // Encrypt plaintext using AES
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_SIZE, iv));
        byte[] encryptedBytes = aesCipher.doFinal(plaintext.getBytes());

        // Encrypt AES key using RSA
        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // Calculate HMAC
        byte[] hmac = calculateHMAC(encryptedBytes, aesKey);

        // Combine encrypted data, encrypted AES key, IV, and HMAC
        String combinedData = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            combinedData = Base64.getEncoder().encodeToString(encryptedBytes) + ":" +
                    Base64.getEncoder().encodeToString(encryptedAesKey) + ":" +
                    Base64.getEncoder().encodeToString(iv) + ":" +
                    Base64.getEncoder().encodeToString(hmac);
        }

        return combinedData;
    }

    public static String decrypt(String combinedData, PrivateKey privateKey) throws Exception {
        // Split combined data
        String[] parts = combinedData.split(":");
        byte[] encryptedBytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encryptedBytes = Base64.getDecoder().decode(parts[0]);
        }
        byte[] encryptedAesKey = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encryptedAesKey = Base64.getDecoder().decode(parts[1]);
        }
        byte[] iv = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            iv = Base64.getDecoder().decode(parts[2]);
        }
        byte[] hmac = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            hmac = Base64.getDecoder().decode(parts[3]);
        }

        // Decrypt AES key using RSA
        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");

        // Verify HMAC
        byte[] calculatedHMAC = calculateHMAC(encryptedBytes, aesKey);
        if (!MessageDigest.isEqual(hmac, calculatedHMAC)) {
            throw new Exception("HMAC verification failed");
        }

        // Decrypt plaintext using AES
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(TAG_SIZE, iv));
        byte[] decryptedBytes = aesCipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }

    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }

    public static byte[] generateIV() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        return iv;
    }

    public static byte[] calculateHMAC(byte[] data, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
        hmac.init(key);
        return hmac.doFinal(data);
    }
}


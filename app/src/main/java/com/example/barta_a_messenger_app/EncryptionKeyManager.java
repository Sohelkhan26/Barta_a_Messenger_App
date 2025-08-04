package com.example.barta_a_messenger_app;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.security.SecureRandom;

/**
 * EncryptionKeyManager - Manages encryption keys for friends
 * This class provides functionality to:
 * 1. Generate random encryption keys for new friend requests
 * 2. Store and retrieve encryption keys from local database
 * 3. Handle key exchange during friend request process
 */
public class EncryptionKeyManager {
    private static final String TAG = "EncryptionKeyManager";
    private static final int KEY_LENGTH = 32; // 256 bits for AES-256
    
    private LocalEncryptionDatabase encryptionDb;
    private Context context;
    
    public EncryptionKeyManager(Context context) {
        this.context = context;
        this.encryptionDb = LocalEncryptionDatabase.getInstance(context);
    }
    
    /**
     * Generates a random encryption key for a new friend
     * @return Base64 encoded encryption key
     */
    public String generateNewEncryptionKey() {
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] keyBytes = new byte[KEY_LENGTH];
            secureRandom.nextBytes(keyBytes);
            
            // Convert to Base64 for easy storage and transmission
            String encryptionKey = Base64.encodeToString(keyBytes, Base64.DEFAULT);
            Log.d(TAG, "Generated new encryption key");
            return encryptionKey;
        } catch (Exception e) {
            Log.e(TAG, "Error generating encryption key: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Stores an encryption key for a specific friend in local database
     * @param friendUid The UID of the friend
     * @param encryptionKey The encryption key to store
     * @return true if successful, false otherwise
     */
    public boolean storeEncryptionKey(String friendUid, String encryptionKey) {
        try {
            boolean result = encryptionDb.insertOrUpdateEncryptionKey(friendUid, encryptionKey);
            if (result) {
                Log.d(TAG, "Stored encryption key for friend: " + friendUid);
            } else {
                Log.e(TAG, "Failed to store encryption key for friend: " + friendUid);
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error storing encryption key: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves the encryption key for a specific friend
     * @param friendUid The UID of the friend
     * @return The encryption key or null if not found
     */
    public String getEncryptionKey(String friendUid) {
        try {
            String key = encryptionDb.getEncryptionKey(friendUid);
            if (key != null) {
                Log.d(TAG, "Retrieved encryption key for friend: " + friendUid);
            } else {
                Log.w(TAG, "No encryption key found for friend: " + friendUid);
            }
            return key;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving encryption key: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Removes an encryption key for a specific friend (when unfriended)
     * @param friendUid The UID of the friend
     * @return true if successful, false otherwise
     */
    public boolean removeEncryptionKey(String friendUid) {
        try {
            boolean result = encryptionDb.deleteEncryptionKey(friendUid);
            if (result) {
                Log.d(TAG, "Removed encryption key for friend: " + friendUid);
            } else {
                Log.e(TAG, "Failed to remove encryption key for friend: " + friendUid);
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error removing encryption key: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if an encryption key exists for a specific friend
     * @param friendUid The UID of the friend
     * @return true if key exists, false otherwise
     */
    public boolean hasEncryptionKey(String friendUid) {
        try {
            String key = encryptionDb.getEncryptionKey(friendUid);
            return key != null && !key.isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Error checking encryption key existence: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Encrypts data using the friend's specific encryption key
     * @param friendUid The UID of the friend
     * @param data The data to encrypt
     * @return Encrypted data or null if encryption fails
     */
    public String encryptForFriend(String friendUid, String data) {
        try {
            String encryptionKey = getEncryptionKey(friendUid);
            if (encryptionKey == null) {
                Log.e(TAG, "No encryption key found for friend: " + friendUid);
                return null;
            }
            
            return CryptoHelper.encrypt(encryptionKey, data);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data for friend " + friendUid + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Decrypts data using the friend's specific encryption key
     * @param friendUid The UID of the friend
     * @param encryptedData The encrypted data to decrypt
     * @return Decrypted data or null if decryption fails
     */
    public String decryptFromFriend(String friendUid, String encryptedData) {
        try {
            String encryptionKey = getEncryptionKey(friendUid);
            if (encryptionKey == null) {
                Log.e(TAG, "No encryption key found for friend: " + friendUid);
                return null;
            }
            
            return CryptoHelper.decrypt(encryptionKey, encryptedData);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data from friend " + friendUid + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generates and stores a new encryption key for a friend request
     * This method is called when sending a friend request to a new user
     * @param friendUid The UID of the friend being added
     * @return The generated encryption key for transmission
     */
    public String generateAndStoreKeyForNewFriend(String friendUid) {
        try {
            String newKey = generateNewEncryptionKey();
            if (newKey != null && storeEncryptionKey(friendUid, newKey)) {
                Log.d(TAG, "Generated and stored new key for friend: " + friendUid);
                return newKey;
            } else {
                Log.e(TAG, "Failed to generate and store key for friend: " + friendUid);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating and storing key for friend: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Closes the database connection
     * Note: With singleton pattern, database will be managed by the system
     */
    public void close() {
        // Database is now managed as singleton, no need to close manually
        // Android will handle database lifecycle appropriately
    }
}

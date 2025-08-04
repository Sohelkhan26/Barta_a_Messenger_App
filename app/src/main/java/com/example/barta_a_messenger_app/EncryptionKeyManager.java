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
     * Generates a random encryption key for a new friend with collision detection
     * @return Base64 encoded encryption key (16 bytes = 128-bit for AES)
     */
    public String generateNewEncryptionKey() {
        int maxAttempts = 100; // Prevent infinite loops
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            try {
                SecureRandom secureRandom = new SecureRandom();
                byte[] keyBytes = new byte[16]; // 16 bytes = 128-bit AES key (more compatible)
                secureRandom.nextBytes(keyBytes);
                
                // Convert to Base64 without newlines for consistent length
                String encryptionKey = Base64.encodeToString(keyBytes, Base64.NO_WRAP);
                
                // Check for collision with existing keys
                if (!isKeyCollision(encryptionKey)) {
                    Log.d(TAG, "Generated new unique encryption key after " + (attempts + 1) + " attempts");
                    return encryptionKey;
                }
                
                attempts++;
                Log.w(TAG, "Key collision detected, regenerating... (attempt " + attempts + ")");
                
            } catch (Exception e) {
                Log.e(TAG, "Error generating encryption key: " + e.getMessage());
                return null;
            }
        }
        
        Log.e(TAG, "Failed to generate unique key after " + maxAttempts + " attempts");
        return null;
    }
    
    /**
     * Checks if a generated key collides with existing keys in local database
     * @param key The key to check for collision
     * @return true if collision detected, false if key is unique
     */
    private boolean isKeyCollision(String key) {
        try {
            // Get all existing keys and check for collision
            return encryptionDb.doesKeyExist(key);
        } catch (Exception e) {
            Log.e(TAG, "Error checking key collision: " + e.getMessage());
            return false; // Assume no collision if error occurs
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
     * For group messages, uses hardcoded key
     * @param friendUid The UID of the friend
     * @param data The data to encrypt
     * @param isGroupMessage Whether this is a group message
     * @return Encrypted data or null if encryption fails
     */
    public String encryptForFriend(String friendUid, String data, boolean isGroupMessage) {
        try {
            // For group messages, use hardcoded key
            if (isGroupMessage) {
                return CryptoHelper.encryptWithFallbackKey(data);
            }
            
            // For individual chats, use friend-specific key
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
     * Encrypts data using the friend's specific encryption key (for backward compatibility)
     * @param friendUid The UID of the friend
     * @param data The data to encrypt
     * @return Encrypted data or null if encryption fails
     */
    public String encryptForFriend(String friendUid, String data) {
        return encryptForFriend(friendUid, data, false);
    }
    
    /**
     * Decrypts data using the friend's specific encryption key
     * For group messages, uses hardcoded key
     * @param friendUid The UID of the friend
     * @param encryptedData The encrypted data to decrypt
     * @param isGroupMessage Whether this is a group message
     * @return Decrypted data or null if decryption fails
     */
    public String decryptFromFriend(String friendUid, String encryptedData, boolean isGroupMessage) {
        try {
            // For group messages, use hardcoded key
            if (isGroupMessage) {
                return CryptoHelper.decryptWithFallbackKey(encryptedData);
            }
            
            // For individual chats, use friend-specific key
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
     * Decrypts data using the friend's specific encryption key (for backward compatibility)
     * @param friendUid The UID of the friend
     * @param encryptedData The encrypted data to decrypt
     * @return Decrypted data or null if decryption fails
     */
    public String decryptFromFriend(String friendUid, String encryptedData) {
        return decryptFromFriend(friendUid, encryptedData, false);
    }
    
    /**
     * Generates and stores a new encryption key for a friend request
     * This method is called when sending a friend request to a new user
     * Also stores the key in Firebase for the friend to retrieve
     * @param friendUid The UID of the friend being added
     * @return The generated encryption key for transmission
     */
    public String generateAndStoreKeyForNewFriend(String friendUid) {
        try {
            String newKey = generateNewEncryptionKey();
            if (newKey != null && storeEncryptionKey(friendUid, newKey)) {
                Log.d(TAG, "Generated and stored new key for friend: " + friendUid);
                
                // Store key in Firebase for friend to retrieve
                storeKeyInFirebase(friendUid, newKey);
                
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
     * Stores encryption key in Firebase for friend to retrieve
     * @param friendUid The friend's UID
     * @param encryptionKey The encryption key to store
     */
    private void storeKeyInFirebase(String friendUid, String encryptionKey) {
        try {
            // Import FirebaseDatabase and DatabaseReference here if needed
            com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance();
            com.google.firebase.database.DatabaseReference keyRef = database.getReference("EncryptionKeys")
                    .child(friendUid)  // Receiver's UID
                    .child(getCurrentUserId()); // Sender's UID
            
            keyRef.setValue(encryptionKey)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Encryption key stored in Firebase for friend: " + friendUid);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to store encryption key in Firebase: " + e.getMessage());
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error storing key in Firebase: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves and stores encryption key from Firebase when accepting friend request
     * @param senderUid The sender's UID (who sent the friend request)
     * @param onComplete Callback when operation completes
     */
    public void retrieveAndStoreKeyFromFirebase(String senderUid, OnKeyRetrievedListener onComplete) {
        try {
            com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance();
            com.google.firebase.database.DatabaseReference keyRef = database.getReference("EncryptionKeys")
                    .child(getCurrentUserId()) // Current user's UID (receiver)
                    .child(senderUid); // Sender's UID
            
            keyRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String encryptionKey = snapshot.getValue(String.class);
                        if (encryptionKey != null && !encryptionKey.isEmpty()) {
                            // Store key locally
                            boolean stored = storeEncryptionKey(senderUid, encryptionKey);
                            if (stored) {
                                // Remove key from Firebase after successful local storage
                                keyRef.removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Encryption key removed from Firebase after local storage");
                                        });
                                Log.d(TAG, "Successfully retrieved and stored encryption key from Firebase");
                                onComplete.onKeyRetrieved(true, encryptionKey);
                            } else {
                                Log.e(TAG, "Failed to store encryption key locally");
                                onComplete.onKeyRetrieved(false, null);
                            }
                        } else {
                            Log.e(TAG, "Empty encryption key received from Firebase");
                            onComplete.onKeyRetrieved(false, null);
                        }
                    } else {
                        Log.w(TAG, "No encryption key found in Firebase for sender: " + senderUid);
                        onComplete.onKeyRetrieved(false, null);
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                    Log.e(TAG, "Failed to retrieve encryption key from Firebase: " + error.getMessage());
                    onComplete.onKeyRetrieved(false, null);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving key from Firebase: " + e.getMessage());
            onComplete.onKeyRetrieved(false, null);
        }
    }
    
    /**
     * Gets current user's UID
     * @return Current user's UID or empty string if not available
     */
    private String getCurrentUserId() {
        try {
            com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
            com.google.firebase.auth.FirebaseUser user = auth.getCurrentUser();
            return user != null ? user.getUid() : "";
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user ID: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Interface for key retrieval callback
     */
    public interface OnKeyRetrievedListener {
        void onKeyRetrieved(boolean success, String encryptionKey);
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

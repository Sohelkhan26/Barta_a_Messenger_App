package com.example.barta_a_messenger_app;

import android.content.Context;
import android.util.Log;

/**
 * EncryptionDemo - Demonstrates the dynamic encryption key system
 * This class shows how the new encryption system works and can be used for testing
 */
public class EncryptionDemo {
    private static final String TAG = "EncryptionDemo";
    
    /**
     * Tests database operations to identify potential issues
     * @param context Application context
     */
    public static void testDatabaseOperations(Context context) {
        Log.d(TAG, "=== Testing Database Operations ===");
        
        EncryptionKeyManager keyManager = new EncryptionKeyManager(context);
        String testFriendUid = "test_friend_" + System.currentTimeMillis();
        
        try {
            // Test 1: Generate a new key
            Log.d(TAG, "Test 1: Generating new encryption key...");
            String newKey = keyManager.generateNewEncryptionKey();
            Log.d(TAG, "Generated key: " + (newKey != null ? "SUCCESS" : "FAILED"));
            
            if (newKey != null) {
                // Test 2: Store the key
                Log.d(TAG, "Test 2: Storing encryption key...");
                boolean stored = keyManager.storeEncryptionKey(testFriendUid, newKey);
                Log.d(TAG, "Stored key: " + (stored ? "SUCCESS" : "FAILED"));
                
                if (stored) {
                    // Test 3: Retrieve the key
                    Log.d(TAG, "Test 3: Retrieving encryption key...");
                    String retrievedKey = keyManager.getEncryptionKey(testFriendUid);
                    Log.d(TAG, "Retrieved key: " + (retrievedKey != null ? "SUCCESS" : "FAILED"));
                    
                    if (retrievedKey != null && retrievedKey.equals(newKey)) {
                        Log.d(TAG, "Key retrieval verification: SUCCESS");
                        
                        // Test 4: Encrypt and decrypt a message
                        Log.d(TAG, "Test 4: Testing encryption/decryption...");
                        String testMessage = "Hello, this is a test message!";
                        String encrypted = keyManager.encryptForFriend(testFriendUid, testMessage);
                        
                        if (encrypted != null) {
                            String decrypted = keyManager.decryptFromFriend(testFriendUid, encrypted);
                            Log.d(TAG, "Encryption/Decryption: " + 
                                (testMessage.equals(decrypted) ? "SUCCESS" : "FAILED"));
                        } else {
                            Log.e(TAG, "Encryption failed");
                        }
                    } else {
                        Log.e(TAG, "Key retrieval verification: FAILED");
                    }
                }
            }
            
            // Test 5: Generate and store key in one operation
            Log.d(TAG, "Test 5: Testing generateAndStoreKeyForNewFriend...");
            String friend2Uid = "test_friend2_" + System.currentTimeMillis();
            String generatedAndStored = keyManager.generateAndStoreKeyForNewFriend(friend2Uid);
            Log.d(TAG, "Generate and store: " + (generatedAndStored != null ? "SUCCESS" : "FAILED"));
            
        } catch (Exception e) {
            Log.e(TAG, "Database test error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up test data
            keyManager.removeEncryptionKey(testFriendUid);
            // Note: We don't close the keyManager since it's now using singleton pattern
        }
        
        Log.d(TAG, "=== Database Operations Test Complete ===");
    }

    /**
     * Demonstrates the complete encryption key lifecycle
     * @param context Application context
     */
    public static void demonstrateEncryptionSystem(Context context) {
        Log.d(TAG, "=== Starting Encryption System Demo ===");
        
        // First, run database operations test to ensure everything works
        testDatabaseOperations(context);
        
        // Initialize the encryption key manager
        EncryptionKeyManager keyManager = new EncryptionKeyManager(context);
        
        // Simulate friend UIDs
        String friend1Uid = "friend_001";
        String friend2Uid = "friend_002";
        String friend3Uid = "friend_003";
        
        // Sample messages
        String message1 = "Hello, this is a secret message!";
        String message2 = "How are you doing today?";
        String message3 = "Let's meet at the coffee shop at 5 PM";
        
        try {
            // === Demonstration 1: Generate and store encryption keys ===
            Log.d(TAG, "--- Demo 1: Generating and storing encryption keys ---");
            
            String key1 = keyManager.generateAndStoreKeyForNewFriend(friend1Uid);
            String key2 = keyManager.generateAndStoreKeyForNewFriend(friend2Uid);
            String key3 = keyManager.generateAndStoreKeyForNewFriend(friend3Uid);
            
            Log.d(TAG, "Generated keys:");
            Log.d(TAG, "Friend 1 key: " + (key1 != null ? "SUCCESS" : "FAILED"));
            Log.d(TAG, "Friend 2 key: " + (key2 != null ? "SUCCESS" : "FAILED"));
            Log.d(TAG, "Friend 3 key: " + (key3 != null ? "SUCCESS" : "FAILED"));
            
            // === Demonstration 2: Encrypt messages for different friends ===
            Log.d(TAG, "--- Demo 2: Encrypting messages with different keys ---");
            
            String encryptedMsg1 = keyManager.encryptForFriend(friend1Uid, message1);
            String encryptedMsg2 = keyManager.encryptForFriend(friend2Uid, message2);
            String encryptedMsg3 = keyManager.encryptForFriend(friend3Uid, message3);
            
            Log.d(TAG, "Original message 1: " + message1);
            Log.d(TAG, "Encrypted for friend 1: " + encryptedMsg1);
            Log.d(TAG, "Original message 2: " + message2);
            Log.d(TAG, "Encrypted for friend 2: " + encryptedMsg2);
            Log.d(TAG, "Original message 3: " + message3);
            Log.d(TAG, "Encrypted for friend 3: " + encryptedMsg3);
            
            // === Demonstration 3: Decrypt messages ===
            Log.d(TAG, "--- Demo 3: Decrypting messages ---");
            
            String decryptedMsg1 = keyManager.decryptFromFriend(friend1Uid, encryptedMsg1);
            String decryptedMsg2 = keyManager.decryptFromFriend(friend2Uid, encryptedMsg2);
            String decryptedMsg3 = keyManager.decryptFromFriend(friend3Uid, encryptedMsg3);
            
            Log.d(TAG, "Decrypted message 1: " + decryptedMsg1);
            Log.d(TAG, "Decrypted message 2: " + decryptedMsg2);
            Log.d(TAG, "Decrypted message 3: " + decryptedMsg3);
            
            // Verify decryption success
            boolean success1 = message1.equals(decryptedMsg1);
            boolean success2 = message2.equals(decryptedMsg2);
            boolean success3 = message3.equals(decryptedMsg3);
            
            Log.d(TAG, "Decryption verification:");
            Log.d(TAG, "Message 1 match: " + success1);
            Log.d(TAG, "Message 2 match: " + success2);
            Log.d(TAG, "Message 3 match: " + success3);
            
            // === Demonstration 4: Cross-friend decryption (should fail) ===
            Log.d(TAG, "--- Demo 4: Cross-friend decryption test (should fail) ---");
            
            String wrongDecrypt1 = keyManager.decryptFromFriend(friend2Uid, encryptedMsg1); // Wrong key
            String wrongDecrypt2 = keyManager.decryptFromFriend(friend3Uid, encryptedMsg2); // Wrong key
            
            Log.d(TAG, "Attempting to decrypt friend 1's message with friend 2's key: " + 
                     (wrongDecrypt1 != null ? "UNEXPECTED SUCCESS" : "CORRECTLY FAILED"));
            Log.d(TAG, "Attempting to decrypt friend 2's message with friend 3's key: " + 
                     (wrongDecrypt2 != null ? "UNEXPECTED SUCCESS" : "CORRECTLY FAILED"));
            
            // === Demonstration 5: Retrieve stored keys ===
            Log.d(TAG, "--- Demo 5: Retrieving stored keys ---");
            
            String retrievedKey1 = keyManager.getEncryptionKey(friend1Uid);
            String retrievedKey2 = keyManager.getEncryptionKey(friend2Uid);
            String retrievedKey3 = keyManager.getEncryptionKey(friend3Uid);
            
            Log.d(TAG, "Retrieved keys match generated keys:");
            Log.d(TAG, "Friend 1: " + (key1 != null && key1.equals(retrievedKey1)));
            Log.d(TAG, "Friend 2: " + (key2 != null && key2.equals(retrievedKey2)));
            Log.d(TAG, "Friend 3: " + (key3 != null && key3.equals(retrievedKey3)));
            
            // === Demonstration 6: Key existence check ===
            Log.d(TAG, "--- Demo 6: Key existence check ---");
            
            boolean hasKey1 = keyManager.hasEncryptionKey(friend1Uid);
            boolean hasKey2 = keyManager.hasEncryptionKey(friend2Uid);
            boolean hasKey3 = keyManager.hasEncryptionKey(friend3Uid);
            boolean hasKeyNonExistent = keyManager.hasEncryptionKey("nonexistent_friend");
            
            Log.d(TAG, "Key existence check:");
            Log.d(TAG, "Friend 1 has key: " + hasKey1);
            Log.d(TAG, "Friend 2 has key: " + hasKey2);
            Log.d(TAG, "Friend 3 has key: " + hasKey3);
            Log.d(TAG, "Nonexistent friend has key: " + hasKeyNonExistent);
            
            // === Demonstration 7: Fallback encryption compatibility ===
            Log.d(TAG, "--- Demo 7: Fallback encryption compatibility ---");
            
            String fallbackEncrypted = CryptoHelper.encryptWithFallbackKey(message1);
            String fallbackDecrypted = CryptoHelper.decryptWithFallbackKey(fallbackEncrypted);
            
            Log.d(TAG, "Fallback encryption test:");
            Log.d(TAG, "Original: " + message1);
            Log.d(TAG, "Fallback encrypted: " + fallbackEncrypted);
            Log.d(TAG, "Fallback decrypted: " + fallbackDecrypted);
            Log.d(TAG, "Fallback encryption works: " + message1.equals(fallbackDecrypted));
            
            // === Demonstration 8: Database statistics ===
            Log.d(TAG, "--- Demo 8: Database statistics ---");
            
            LocalEncryptionDatabase encDb = LocalEncryptionDatabase.getInstance(context);
            int keyCount = encDb.getEncryptionKeyCount();
            Log.d(TAG, "Total encryption keys stored: " + keyCount);
            // Note: Don't close singleton database instance
            
            // === Demonstration 9: Remove a key ===
            Log.d(TAG, "--- Demo 9: Remove encryption key ---");
            
            boolean removed = keyManager.removeEncryptionKey(friend3Uid);
            boolean stillExists = keyManager.hasEncryptionKey(friend3Uid);
            
            Log.d(TAG, "Removed friend 3's key: " + removed);
            Log.d(TAG, "Friend 3's key still exists: " + stillExists);
            
            Log.d(TAG, "=== Encryption System Demo Complete ===");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during encryption demo: " + e.getMessage(), e);
        } finally {
            // Always close the key manager
            keyManager.close();
        }
    }
    
    /**
     * Demonstrates the friend request flow with encryption keys
     * @param context Application context
     */
    public static void demonstrateFriendRequestFlow(Context context) {
        Log.d(TAG, "=== Friend Request Flow Demo ===");
        
        EncryptionKeyManager senderKeyManager = new EncryptionKeyManager(context);
        EncryptionKeyManager receiverKeyManager = new EncryptionKeyManager(context);
        
        String senderUid = "sender_123";
        String receiverUid = "receiver_456";
        
        try {
            // Step 1: Sender generates encryption key for new friend request
            Log.d(TAG, "Step 1: Sender generates encryption key");
            String encryptionKey = senderKeyManager.generateAndStoreKeyForNewFriend(receiverUid);
            
            if (encryptionKey != null) {
                Log.d(TAG, "Encryption key generated successfully");
                
                // Step 2: Create friend request with encryption key (simulated)
                Log.d(TAG, "Step 2: Creating friend request with encryption key");
                Request friendRequest = new Request("John Doe", "+1234567890", senderUid, receiverUid, "pending", encryptionKey);
                
                // Step 3: Receiver accepts friend request and stores the encryption key
                Log.d(TAG, "Step 3: Receiver accepts friend request and stores encryption key");
                boolean keyStored = receiverKeyManager.storeEncryptionKey(senderUid, friendRequest.getEncryptionKey());
                
                if (keyStored) {
                    Log.d(TAG, "Encryption key stored by receiver");
                    
                    // Step 4: Test bi-directional encrypted communication
                    Log.d(TAG, "Step 4: Testing bi-directional encrypted communication");
                    
                    String message1 = "Hello from sender!";
                    String message2 = "Hi there, received your message!";
                    
                    // Sender encrypts message for receiver
                    String encryptedFromSender = senderKeyManager.encryptForFriend(receiverUid, message1);
                    // Receiver decrypts message from sender
                    String decryptedBySender = receiverKeyManager.decryptFromFriend(senderUid, encryptedFromSender);
                    
                    // Receiver encrypts reply for sender
                    String encryptedFromReceiver = receiverKeyManager.encryptForFriend(senderUid, message2);
                    // Sender decrypts reply from receiver
                    String decryptedByReceiver = senderKeyManager.decryptFromFriend(receiverUid, encryptedFromReceiver);
                    
                    Log.d(TAG, "Sender -> Receiver: " + message1 + " -> " + decryptedBySender);
                    Log.d(TAG, "Communication successful: " + message1.equals(decryptedBySender));
                    
                    Log.d(TAG, "Receiver -> Sender: " + message2 + " -> " + decryptedByReceiver);
                    Log.d(TAG, "Reply successful: " + message2.equals(decryptedByReceiver));
                    
                } else {
                    Log.e(TAG, "Failed to store encryption key");
                }
            } else {
                Log.e(TAG, "Failed to generate encryption key");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error during friend request flow demo: " + e.getMessage(), e);
        } finally {
            senderKeyManager.close();
            receiverKeyManager.close();
        }
        
        Log.d(TAG, "=== Friend Request Flow Demo Complete ===");
    }
}

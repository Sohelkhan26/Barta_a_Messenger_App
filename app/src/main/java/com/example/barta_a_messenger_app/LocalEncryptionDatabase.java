package com.example.barta_a_messenger_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * LocalEncryptionDatabase - SQLite database helper for storing encryption keys locally
 * This database stores encryption keys for each friend contact to enable secure communication
 */
public class LocalEncryptionDatabase extends SQLiteOpenHelper {
    private static final String TAG = "LocalEncryptionDatabase";
    
    // Database configuration
    private static final String DATABASE_NAME = "encryption_keys.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table and column names
    private static final String TABLE_ENCRYPTION_KEYS = "encryption_keys";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FRIEND_UID = "friend_uid";
    private static final String COLUMN_ENCRYPTION_KEY = "encryption_key";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    
    // Create table SQL
    private static final String CREATE_ENCRYPTION_KEYS_TABLE = 
        "CREATE TABLE " + TABLE_ENCRYPTION_KEYS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_FRIEND_UID + " TEXT UNIQUE NOT NULL, " +
        COLUMN_ENCRYPTION_KEY + " TEXT NOT NULL, " +
        COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
        COLUMN_UPDATED_AT + " INTEGER NOT NULL" +
        ");";
    
    // Singleton instance
    private static LocalEncryptionDatabase instance;
    private static final Object lock = new Object();
    
    private LocalEncryptionDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static LocalEncryptionDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new LocalEncryptionDatabase(context.getApplicationContext());
                }
            }
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_ENCRYPTION_KEYS_TABLE);
            Log.d(TAG, "Encryption keys table created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating encryption keys table: " + e.getMessage());
        }
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENCRYPTION_KEYS);
            onCreate(db);
            Log.d(TAG, "Encryption keys database upgraded from version " + oldVersion + " to " + newVersion);
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading encryption keys database: " + e.getMessage());
        }
    }
    
    /**
     * Inserts or updates an encryption key for a friend
     * @param friendUid The UID of the friend
     * @param encryptionKey The encryption key to store
     * @return true if successful, false otherwise
     */
    public boolean insertOrUpdateEncryptionKey(String friendUid, String encryptionKey) {
        if (friendUid == null || friendUid.isEmpty() || encryptionKey == null || encryptionKey.isEmpty()) {
            Log.e(TAG, "Invalid parameters for insertOrUpdateEncryptionKey");
            return false;
        }
        
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getWritableDatabase();
            
            // Check if record already exists using the same database connection
            cursor = db.query(
                TABLE_ENCRYPTION_KEYS,
                new String[]{COLUMN_ENCRYPTION_KEY},
                COLUMN_FRIEND_UID + " = ?",
                new String[]{friendUid},
                null, null, null
            );
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_FRIEND_UID, friendUid);
            values.put(COLUMN_ENCRYPTION_KEY, encryptionKey);
            values.put(COLUMN_UPDATED_AT, System.currentTimeMillis());
            
            boolean recordExists = cursor != null && cursor.moveToFirst();
            
            if (recordExists) {
                // Update existing record
                int rowsAffected = db.update(
                    TABLE_ENCRYPTION_KEYS, 
                    values, 
                    COLUMN_FRIEND_UID + " = ?", 
                    new String[]{friendUid}
                );
                
                if (rowsAffected > 0) {
                    Log.d(TAG, "Updated encryption key for friend: " + friendUid);
                    return true;
                } else {
                    Log.e(TAG, "Failed to update encryption key for friend: " + friendUid);
                    return false;
                }
            } else {
                // Insert new record
                values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
                
                long result = db.insert(TABLE_ENCRYPTION_KEYS, null, values);
                
                if (result != -1) {
                    Log.d(TAG, "Inserted new encryption key for friend: " + friendUid);
                    return true;
                } else {
                    Log.e(TAG, "Failed to insert encryption key for friend: " + friendUid);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting/updating encryption key: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Don't close the database - let it be managed by the system
        }
    }
    
    /**
     * Retrieves the encryption key for a specific friend
     * @param friendUid The UID of the friend
     * @return The encryption key or null if not found
     */
    public String getEncryptionKey(String friendUid) {
        if (friendUid == null || friendUid.isEmpty()) {
            Log.e(TAG, "Invalid friendUid for getEncryptionKey");
            return null;
        }
        
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            
            cursor = db.query(
                TABLE_ENCRYPTION_KEYS,
                new String[]{COLUMN_ENCRYPTION_KEY},
                COLUMN_FRIEND_UID + " = ?",
                new String[]{friendUid},
                null, null, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                String encryptionKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTION_KEY));
                Log.d(TAG, "Retrieved encryption key for friend: " + friendUid);
                return encryptionKey;
            } else {
                Log.w(TAG, "No encryption key found for friend: " + friendUid);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving encryption key: " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Don't close the database - let it be managed by the system
        }
    }
    
    /**
     * Deletes an encryption key for a specific friend
     * @param friendUid The UID of the friend
     * @return true if successful, false otherwise
     */
    public boolean deleteEncryptionKey(String friendUid) {
        if (friendUid == null || friendUid.isEmpty()) {
            Log.e(TAG, "Invalid friendUid for deleteEncryptionKey");
            return false;
        }
        
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            
            int rowsDeleted = db.delete(
                TABLE_ENCRYPTION_KEYS,
                COLUMN_FRIEND_UID + " = ?",
                new String[]{friendUid}
            );
            
            if (rowsDeleted > 0) {
                Log.d(TAG, "Deleted encryption key for friend: " + friendUid);
                return true;
            } else {
                Log.w(TAG, "No encryption key found to delete for friend: " + friendUid);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting encryption key: " + e.getMessage());
            return false;
        } finally {
            // Don't close the database - let it be managed by the system
        }
    }
    
    /**
     * Gets the total count of stored encryption keys
     * @return The number of encryption keys stored
     */
    public int getEncryptionKeyCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ENCRYPTION_KEYS, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                Log.d(TAG, "Total encryption keys stored: " + count);
                return count;
            }
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting encryption key count: " + e.getMessage());
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Don't close the database - let it be managed by the system
        }
    }
    
    /**
     * Checks if a specific encryption key already exists in the database (for collision detection)
     * @param encryptionKey The encryption key to check
     * @return true if key exists, false otherwise
     */
    public boolean doesKeyExist(String encryptionKey) {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            return false;
        }
        
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            
            cursor = db.query(
                TABLE_ENCRYPTION_KEYS,
                new String[]{COLUMN_FRIEND_UID},
                COLUMN_ENCRYPTION_KEY + " = ?",
                new String[]{encryptionKey},
                null, null, null
            );
            
            boolean exists = cursor != null && cursor.moveToFirst();
            if (exists) {
                Log.w(TAG, "Encryption key collision detected");
            }
            return exists;
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking key existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Don't close the database - let it be managed by the system
        }
    }
    
    /**
     * Clears all encryption keys (use with caution)
     * @return true if successful, false otherwise
     */
    public boolean clearAllEncryptionKeys() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_ENCRYPTION_KEYS, null, null);
            
            Log.d(TAG, "Cleared " + rowsDeleted + " encryption keys");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all encryption keys: " + e.getMessage());
            return false;
        } finally {
            // Don't close the database - let it be managed by the system
        }
    }
}

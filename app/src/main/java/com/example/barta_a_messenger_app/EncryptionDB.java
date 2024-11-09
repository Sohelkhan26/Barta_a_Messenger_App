package com.example.barta_a_messenger_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class EncryptionDB extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "encryptionKeys.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "ENCRYPTION_KEYS";
    private static final String COLUMN_FRIEND_ID = "FRIEND_ID";
    private static final String COLUMN_ENCRYPTION_KEY = "ENCRYPTION_KEY";
    public EncryptionDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
        this.context = context; // This is the context of the activity that is calling the database
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // surround with try catch block in the future
        String createTableStatement = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_FRIEND_ID + " TEXT PRIMARY KEY, " + COLUMN_ENCRYPTION_KEY + " TEXT)"; // Text datatype ম্যাক্স কত লেংথ এর হয়? সাইজ এক্সিড করলে এরর দিবে না তো?
        try {
            sqLiteDatabase.execSQL(createTableStatement);
        } catch (SQLException e) {
            Toast.makeText(context, "E: " +  e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion , int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public String addFriendKey(String friendId) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String encryptionKey = CryptoHelper.generateUniqueKey(); // This generated key may not be unique. May be upgraded in the future.
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIEND_ID, friendId);
        values.put(COLUMN_ENCRYPTION_KEY, encryptionKey);
        try {
            sqLiteDatabase.insert(COLUMN_FRIEND_ID, null, values);
        } catch (Exception e) {
            Toast.makeText(context, "Send Friend Request encryption key failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sqLiteDatabase.close();
        return encryptionKey; // is it necessary to return the key?
    }

    public String getFriendKey(String friendId) { // Receiver ID should be passed
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, new String[]{COLUMN_ENCRYPTION_KEY},
                COLUMN_FRIEND_ID + "=?", new String[]{friendId},
                null, null, null);
        // Table name , columns to return , where clause -> SQL query Structure
        if (cursor != null && cursor.moveToFirst()) { // query returned at least one row
            String key = cursor.getString(0);
            cursor.close();
            return key;
        }
        else{
            Toast.makeText(context, "No key found for friend: " + friendId, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public void insert(String friendId , String encryptionKey){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIEND_ID, friendId);
        values.put(COLUMN_ENCRYPTION_KEY, encryptionKey);
        try {
            sqLiteDatabase.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to add encryption key in sqlite DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sqLiteDatabase.close();
    }

}

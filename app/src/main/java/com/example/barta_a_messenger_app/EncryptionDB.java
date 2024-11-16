package com.example.barta_a_messenger_app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class EncryptionDB extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "encryptionKeys.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "ENCRYPTION_KEYS";
    private static final String COLUMN_FRIEND_ID = "FRIEND_ID";
    private static final String COLUMN_ENCRYPTION_KEY = "ENCRYPTION_KEY";
    private static final String TAG = "EncryptionDB";
    public EncryptionDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
        this.context = context; // This is the context of the activity that is calling the database
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // surround with try catch block in the future
        String createTableStatement = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_FRIEND_ID + " TEXT PRIMARY KEY, " + COLUMN_ENCRYPTION_KEY + " TEXT)"; // Text datatype ম্যাক্স কত লেংথ এর হয়? সাইজ এক্সিড করলে এরর দিবে না তো?
        try {
            sqLiteDatabase.execSQL(createTableStatement);
        } catch (SQLException e) {
            Log.d(TAG, "onCreate failed: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion , int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public String addFriendKey(String friendId) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (!isTableExists(sqLiteDatabase, TABLE_NAME)) {
            onCreate(sqLiteDatabase);
        }
        Cursor cursor = friendExists(friendId);
        if(cursor.getCount() > 0 && cursor.moveToFirst()){ // আগেও যদি একই নাম্বার ফ্রেন্ড রিকোয়েস্ট পাঠানো হয়ে থাকে তাহলে এরর দিবে। কারণ টেবিলের ফ্রেন্ড আইডি প্রাইমারি কি হিসেবে ডিফাইন করা। এরর যাতে না দেয় তাই আগেই চেক করা হইছে।
            String key = getFriendKey(friendId); cursor.getString(1); // 0 column -> friendId , 1 column -> encryptionKey
            cursor.close();
            return key;
        }
        String encryptionKey = CryptoHelper.generateUniqueKey(); // This generated key may not be unique. May be upgraded in the future.
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIEND_ID, friendId);
        values.put(COLUMN_ENCRYPTION_KEY, encryptionKey);
        try {
            sqLiteDatabase.insert(TABLE_NAME, null, values);
            Log.d(TAG, "friend key added successfully");
        } catch (Exception e) {
            Log.d(TAG, "addFriendKey error: " + e.getMessage());
        }
        sqLiteDatabase.close();
        return encryptionKey; // is it necessary to return the key?
    }
    private Cursor friendExists(String friendId){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT 1 FROM " + TABLE_NAME + " WHERE " + COLUMN_FRIEND_ID + " = ?", new String[]{friendId});
        return cursor;
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
            Log.d(TAG, "getFriendKey: no key found for friend: " + friendId);
        }
        return null;
    }

    public void insert(String friendId , String encryptionKey){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        if (!isTableExists(sqLiteDatabase, TABLE_NAME)) {
            onCreate(sqLiteDatabase);
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIEND_ID, friendId);
        values.put(COLUMN_ENCRYPTION_KEY, encryptionKey);
        try {
            sqLiteDatabase.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.d(TAG, "friend key insertion failed : " + e.getMessage());
        }
        sqLiteDatabase.close();
    }
    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

}

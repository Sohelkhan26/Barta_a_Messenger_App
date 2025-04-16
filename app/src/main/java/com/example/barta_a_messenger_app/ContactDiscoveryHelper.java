package com.example.barta_a_messenger_app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class ContactDiscoveryHelper {

    private static final String TAG = "ContactDiscoveryHelper";

    public static List<String> getHashedPhoneNumbers(Context context) {
        List<String> hashedNumbers = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        try {
            Cursor cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                Log.d(TAG, "Found " + cursor.getCount() + " contacts");
                if (cursor.moveToFirst()) {
                    do {
                        String phoneNumber = cursor.getString(cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (phoneNumber != null) {
                            // Clean the phone number - remove all non-digits
                            String originalNumber = phoneNumber;
                            phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");
                            Log.d(TAG, "Original number: " + originalNumber);
                            Log.d(TAG, "After cleaning: " + phoneNumber);

                            // Add country code if not present
                            if (!phoneNumber.startsWith("+880")) {
                                if (phoneNumber.startsWith("0")) {
                                    phoneNumber = "+88" + phoneNumber;
                                } else {
                                    phoneNumber = "+880" + phoneNumber;
                                }
                            }

                            Log.d(TAG, "After country code: " + phoneNumber);
                            String hashedNumber = hashPhoneNumber(phoneNumber);
                            Log.d(TAG, "Hashed contact number: " + hashedNumber);
                            if (hashedNumber != null) {
                                hashedNumbers.add(hashedNumber);
                            }
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
                Log.d(TAG, "Processed " + hashedNumbers.size() + " phone numbers");
            } else {
                Log.e(TAG, "Cursor is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contacts: " + e.getMessage());
            e.printStackTrace();
        }

        return hashedNumbers;
    }

    public static String hashPhoneNumber(String phoneNumber) {
        try {
            Log.d(TAG, "Hashing phone number: " + phoneNumber);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(phoneNumber.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String result = hexString.toString();
            Log.d(TAG, "Hash result: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error hashing phone number: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

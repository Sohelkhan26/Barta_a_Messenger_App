package com.example.barta_a_messenger_app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A utility for performing read/write operations on Drive files via the REST
 * API and opening a file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {

    private static final Log log = LogFactory.getLog(DriveServiceHelper.class);
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private final Context context;

    public DriveServiceHelper(Drive driveService, Context context) {
        mDriveService = driveService;
        this.context = context;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file
     * ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of
     * its name and contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name}
     * and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's
     * My Drive.
     *
     * <p>
     * The returned list will only contain files visible to this app, i.e. those
     * which were created by this app. To perform operations on files not
     * created by the app, the project must request Drive Full Scope in the
     * <a href="https://play.google.com/apps/publish">Google Developer's
     * Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, ()
                -> mDriveService.files().list().setSpaces("drive").execute());
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file
     * picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework
     * {@link Intent} created by {@link #createFilePickerIntent()} using the
     * given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }

    public Task<String> createFileWithContent(String name, String mimeType, ByteArrayContent content) {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType(mimeType)
                    .setName(name);

            File googleFile = mDriveService.files().create(metadata, content).execute();
            if (googleFile == null) {
                android.util.Log.d("Drive Service Helper ", "createFileWithContent: Null result when requesting file creation.");
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    public Task<GoogleDriveFileHolder> createFolder(String folderName, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {

            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {

                root = Collections.singletonList("root");

            } else {

                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName(folderName);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                android.util.Log.d("Drive Service Helper ", "createFolder: Null result when requesting file creation.");
            }
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }

//    public Task<File> uploadFile(Uri fileUri, String fileName) {
//        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
//            // Get the MIME type of the file
//            String mimeType = context.getContentResolver().getType(fileUri);
//
//            // Open InputStream to the file
//            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
//
//            // Create the metadata for the file to upload
//            File fileMetadata = new File();
//            fileMetadata.setName(fileName);  // Set the file name (or any other metadata)
//
//            // Use InputStreamContent instead of FileContent for stream-based uploads
//            InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);
//
//            // Upload the file to Google Drive
//            return mDriveService.files().create(fileMetadata, mediaContent)
//                    .setFields("id")
//                    .execute();
//        });
//    }
    public Task<File> uploadFile(Uri fileUri, String fileName) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            // Get the MIME type of the file
            String mimeType = context.getContentResolver().getType(fileUri);

            // Open InputStream to the file
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);

            // Create the metadata for the file to upload
            File fileMetadata = new File();
            fileMetadata.setName(fileName);  // Set the file name (or any other metadata)

            // Use InputStreamContent instead of FileContent for stream-based uploads
            InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

            // Upload the file to Google Drive
            File uploadedFile = mDriveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            // After the file is uploaded, make it accessible to anyone as a reader
            String fileId = uploadedFile.getId();
            makeFilePublic(fileId);  // Call the method to set file permission

            // Return the uploaded file with its ID
            return uploadedFile;
        });
    }

    private void makeFilePublic(String fileId) {
        try {
            // Create a new permission for the file, setting the type to "anyone" and the role to "reader"
            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader");  // "reader" role grants read access to anyone

            // Apply the permission to the uploaded file
            mDriveService.permissions().create(fileId, permission).execute();

            android.util.Log.d("File Permission", "File made public successfully");
        } catch (IOException e) {
            android.util.Log.d(TAG, "makeFilePublic:    Error setting file permission: " + e.getMessage());
        }
    }

}

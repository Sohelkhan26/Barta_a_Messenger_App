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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    // Cache for folder IDs to avoid repeated API calls
    private Map<String, String> folderCache = new HashMap<>();
    
    // Constants for folder names
    private static final String MAIN_FOLDER_NAME = "Baarta";
    private static final String PDF_FOLDER_NAME = "pdf";
    private static final String IMAGE_FOLDER_NAME = "image";
    private static final String VOICE_FOLDER_NAME = "voice";
    private static final String DOC_FOLDER_NAME = "doc";

    public DriveServiceHelper(Drive driveService, Context context) {
        mDriveService = driveService;
        this.context = context;
        
        // Initialize folder structure in background
        initializeFolderStructure();
    }

    /**
     * Initializes the Baarta folder structure in the background
     */
    private void initializeFolderStructure() {
        // Don't initialize immediately in constructor to avoid blocking
        // The folder structure will be created when first needed during upload
        android.util.Log.d("DriveServiceHelper", "DriveServiceHelper initialized, folder structure will be created on first upload");
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
        intent.setType("*/*"); // Allow all file types

        return intent;
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file
     * picker with a specific MIME type filter.
     */
    public Intent createFilePickerIntent(String mimeType) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);

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

    /**
     * Creates or gets the main Baarta folder structure
     */
    private Task<String> ensureBaartaFolderStructure() {
        return Tasks.call(mExecutor, () -> {
            try {
                // Check if main folder is already cached
                if (folderCache.containsKey(MAIN_FOLDER_NAME)) {
                    String cachedId = folderCache.get(MAIN_FOLDER_NAME);
                    android.util.Log.d("DriveServiceHelper", "Using cached main folder ID: " + cachedId);
                    return cachedId;
                }

                // Search for existing Baarta folder
                String mainFolderId = findFolderByName(MAIN_FOLDER_NAME, "root");
                
                if (mainFolderId == null) {
                    // Create main Baarta folder
                    mainFolderId = createFolderSync(MAIN_FOLDER_NAME, "root");
                    android.util.Log.d("DriveServiceHelper", "Created main Baarta folder with ID: " + mainFolderId);
                } else {
                    android.util.Log.d("DriveServiceHelper", "Found existing main Baarta folder with ID: " + mainFolderId);
                }
                
                // Cache the main folder ID
                folderCache.put(MAIN_FOLDER_NAME, mainFolderId);
                
                // Ensure all subfolders exist
                ensureSubfolder(PDF_FOLDER_NAME, mainFolderId);
                ensureSubfolder(IMAGE_FOLDER_NAME, mainFolderId);
                ensureSubfolder(VOICE_FOLDER_NAME, mainFolderId);
                ensureSubfolder(DOC_FOLDER_NAME, mainFolderId);
                
                android.util.Log.d("DriveServiceHelper", "Folder structure ensured successfully");
                return mainFolderId;
            } catch (Exception e) {
                android.util.Log.e("DriveServiceHelper", "Error ensuring folder structure: " + e.getMessage(), e);
                throw e;
            }
        });
    }

    /**
     * Ensures a subfolder exists under the parent folder
     */
    private void ensureSubfolder(String folderName, String parentFolderId) throws IOException {
        String cacheKey = parentFolderId + "/" + folderName;
        
        android.util.Log.d("DriveServiceHelper", "Ensuring subfolder '" + folderName + "' exists under parent: " + parentFolderId);
        
        if (folderCache.containsKey(cacheKey)) {
            android.util.Log.d("DriveServiceHelper", "Subfolder '" + folderName + "' already cached with ID: " + folderCache.get(cacheKey));
            return; // Already exists and cached
        }
        
        String subfolderId = findFolderByName(folderName, parentFolderId);
        
        if (subfolderId == null) {
            // Create the subfolder
            subfolderId = createFolderSync(folderName, parentFolderId);
            android.util.Log.d("DriveServiceHelper", "Created subfolder '" + folderName + "' with ID: " + subfolderId);
        } else {
            android.util.Log.d("DriveServiceHelper", "Found existing subfolder '" + folderName + "' with ID: " + subfolderId);
        }
        
        // Cache the subfolder ID
        folderCache.put(cacheKey, subfolderId);
        android.util.Log.d("DriveServiceHelper", "Cached subfolder '" + folderName + "' with cache key: " + cacheKey + " and ID: " + subfolderId);
    }

    /**
     * Synchronously creates a folder and returns its ID
     */
    private String createFolderSync(String folderName, String parentFolderId) throws IOException {
        File metadata = new File()
                .setParents(Collections.singletonList(parentFolderId))
                .setMimeType("application/vnd.google-apps.folder")
                .setName(folderName);

        File googleFile = mDriveService.files().create(metadata).execute();
        if (googleFile == null) {
            throw new IOException("Failed to create folder: " + folderName);
        }
        
        return googleFile.getId();
    }

    /**
     * Finds a folder by name under a specific parent
     */
    private String findFolderByName(String folderName, String parentFolderId) throws IOException {
        String query = "mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and '" + parentFolderId + "' in parents and trashed=false";
        
        FileList result = mDriveService.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .execute();
        
        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }
        
        return null;
    }

    /**
     * Determines the appropriate folder based on file MIME type
     */
    private String getFolderForMimeType(String mimeType, String fileName) {
        String mainFolderId = folderCache.get(MAIN_FOLDER_NAME);
        if (mainFolderId == null) {
            android.util.Log.e("DriveServiceHelper", "Main folder not found in cache");
            return null;
        }
        
        String cacheKey;
        
        android.util.Log.d("DriveServiceHelper", "Determining folder for MIME type: " + mimeType + ", fileName: " + fileName);
        
        if (mimeType == null) {
            android.util.Log.d("DriveServiceHelper", "MIME type is null, checking filename for voice patterns");
            // Check if it's a voice file based on filename
            if (fileName != null && (fileName.contains("voice_message") || 
                                   fileName.toLowerCase().endsWith(".m4a") ||
                                   fileName.toLowerCase().endsWith(".mp3") ||
                                   fileName.toLowerCase().endsWith(".wav") ||
                                   fileName.toLowerCase().endsWith(".ogg") ||
                                   fileName.toLowerCase().endsWith(".3gp") ||
                                   fileName.toLowerCase().endsWith(".amr") ||
                                   fileName.toLowerCase().endsWith(".aac") ||
                                   fileName.toLowerCase().endsWith(".wma"))) {
                android.util.Log.d("DriveServiceHelper", "Voice file detected by filename, using voice folder");
                cacheKey = mainFolderId + "/" + VOICE_FOLDER_NAME;
            } else {
                android.util.Log.d("DriveServiceHelper", "MIME type is null, using doc folder");
                cacheKey = mainFolderId + "/" + DOC_FOLDER_NAME;
            }
        } else if (mimeType.startsWith("image/")) {
            android.util.Log.d("DriveServiceHelper", "Image file detected, using image folder");
            cacheKey = mainFolderId + "/" + IMAGE_FOLDER_NAME;
        } else if (mimeType.equals("application/pdf")) {
            android.util.Log.d("DriveServiceHelper", "PDF file detected, using pdf folder");
            cacheKey = mainFolderId + "/" + PDF_FOLDER_NAME;
        } else if (mimeType.startsWith("audio/") || 
                   mimeType.contains("audio") || 
                   mimeType.equals("audio/mpeg") ||
                   mimeType.equals("audio/mp4") ||
                   mimeType.equals("audio/wav") ||
                   mimeType.equals("audio/ogg") ||
                   mimeType.equals("audio/3gpp") ||
                   mimeType.equals("audio/amr") ||
                   mimeType.equals("audio/webm") ||
                   mimeType.equals("audio/aac") ||
                   mimeType.equals("audio/mp3") ||
                   (fileName != null && (fileName.toLowerCase().endsWith(".mp3") ||
                   fileName.toLowerCase().endsWith(".wav") ||
                   fileName.toLowerCase().endsWith(".ogg") ||
                   fileName.toLowerCase().endsWith(".m4a") ||
                   fileName.toLowerCase().endsWith(".3gp") ||
                   fileName.toLowerCase().endsWith(".amr") ||
                   fileName.toLowerCase().endsWith(".aac") ||
                   fileName.toLowerCase().endsWith(".wma")))) {
            android.util.Log.d("DriveServiceHelper", "Audio file detected, using voice folder");
            cacheKey = mainFolderId + "/" + VOICE_FOLDER_NAME;
        } else if (mimeType.startsWith("video/")) {
            android.util.Log.d("DriveServiceHelper", "Video file detected, using doc folder");
            cacheKey = mainFolderId + "/" + DOC_FOLDER_NAME;
        } else if (mimeType.contains("document") || 
                   mimeType.contains("text") || 
                   mimeType.contains("msword") || 
                   mimeType.contains("officedocument") ||
                   mimeType.equals("application/msword") ||
                   mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                   mimeType.equals("application/vnd.ms-excel") ||
                   mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                   mimeType.equals("application/vnd.ms-powerpoint") ||
                   mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                   mimeType.equals("application/rtf") ||
                   mimeType.equals("text/plain") ||
                   mimeType.equals("text/html") ||
                   mimeType.equals("text/csv") ||
                   (fileName != null && (fileName.toLowerCase().endsWith(".doc") ||
                   fileName.toLowerCase().endsWith(".docx") ||
                   fileName.toLowerCase().endsWith(".xls") ||
                   fileName.toLowerCase().endsWith(".xlsx") ||
                   fileName.toLowerCase().endsWith(".ppt") ||
                   fileName.toLowerCase().endsWith(".pptx") ||
                   fileName.toLowerCase().endsWith(".rtf") ||
                   fileName.toLowerCase().endsWith(".txt") ||
                   fileName.toLowerCase().endsWith(".csv") ||
                   fileName.toLowerCase().endsWith(".html") ||
                   fileName.toLowerCase().endsWith(".htm")))) {
            android.util.Log.d("DriveServiceHelper", "Document file detected, using doc folder");
            cacheKey = mainFolderId + "/" + DOC_FOLDER_NAME;
        } else {
            android.util.Log.d("DriveServiceHelper", "Unknown file type (" + mimeType + "), using doc folder");
            cacheKey = mainFolderId + "/" + DOC_FOLDER_NAME;
        }
        
        String folderId = folderCache.get(cacheKey);
        if (folderId == null) {
            android.util.Log.e("DriveServiceHelper", "Subfolder not found in cache for key: " + cacheKey);
        } else {
            android.util.Log.d("DriveServiceHelper", "Using folder ID: " + folderId + " for cache key: " + cacheKey);
        }
        
        return folderId;
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
        return ensureBaartaFolderStructure().continueWithTask(task -> {
            return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
                // Get the MIME type of the file
                String mimeType = context.getContentResolver().getType(fileUri);
                
                // Manual MIME type detection for common audio formats when system fails
                if (mimeType == null && fileName != null) {
                    String lowerFileName = fileName.toLowerCase();
                    if (lowerFileName.endsWith(".m4a")) {
                        mimeType = "audio/mp4";
                        android.util.Log.d("DriveServiceHelper", "Manually set MIME type for .m4a file: " + mimeType);
                    } else if (lowerFileName.endsWith(".mp3")) {
                        mimeType = "audio/mpeg";
                    } else if (lowerFileName.endsWith(".wav")) {
                        mimeType = "audio/wav";
                    } else if (lowerFileName.endsWith(".ogg")) {
                        mimeType = "audio/ogg";
                    } else if (lowerFileName.endsWith(".3gp")) {
                        mimeType = "audio/3gpp";
                    } else if (lowerFileName.endsWith(".amr")) {
                        mimeType = "audio/amr";
                    } else if (lowerFileName.endsWith(".aac")) {
                        mimeType = "audio/aac";
                    }
                }
                
                // Debug logging for voice files
                android.util.Log.d("DriveServiceHelper", "=== UPLOAD DEBUG ===");
                android.util.Log.d("DriveServiceHelper", "File URI: " + fileUri.toString());
                android.util.Log.d("DriveServiceHelper", "File name: " + fileName);
                android.util.Log.d("DriveServiceHelper", "Detected MIME type: " + mimeType);
                android.util.Log.d("DriveServiceHelper", "==================");

                // Open InputStream to the file
                InputStream inputStream = context.getContentResolver().openInputStream(fileUri);

                // Determine the appropriate folder based on file type
                String targetFolderId = null;
                if (task.isSuccessful()) {
                    targetFolderId = getFolderForMimeType(mimeType, fileName);
                }
                
                // Special handling for voice messages when MIME type detection fails
                if (targetFolderId == null && fileName != null && fileName.contains("voice_message")) {
                    android.util.Log.d("DriveServiceHelper", "Voice message detected by filename, forcing to voice folder");
                    String mainFolderId = folderCache.get(MAIN_FOLDER_NAME);
                    if (mainFolderId != null) {
                        targetFolderId = folderCache.get(mainFolderId + "/" + VOICE_FOLDER_NAME);
                        android.util.Log.d("DriveServiceHelper", "Using voice folder: " + targetFolderId);
                    }
                }
                
                // Fallback to root if folder structure creation failed or folder not found
                if (targetFolderId == null) {
                    android.util.Log.w("DriveServiceHelper", "Could not determine target folder, uploading to root directory");
                    targetFolderId = "root";
                }
                
                // Create the metadata for the file to upload
                File fileMetadata = new File();
                fileMetadata.setName(fileName);  // Set the file name
                fileMetadata.setParents(Collections.singletonList(targetFolderId)); // Set parent folder

                // Use InputStreamContent instead of FileContent for stream-based uploads
                InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

                // Upload the file to Google Drive in the appropriate folder
                File uploadedFile = mDriveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                // After the file is uploaded, make it accessible to anyone as a reader
                String fileId = uploadedFile.getId();
                makeFilePublic(fileId);  // Call the method to set file permission

                android.util.Log.d("DriveServiceHelper", "File '" + fileName + "' uploaded successfully with MIME type: " + mimeType + " to folder: " + targetFolderId);

                // Return the uploaded file with its ID
                return uploadedFile;
            });
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

    /**
     * Public method to manually ensure folder structure exists
     * Can be called before uploading files if needed
     */
    public Task<String> createBaartaFolderStructure() {
        return ensureBaartaFolderStructure();
    }

    /**
     * Gets information about the current folder structure
     */
    public Map<String, String> getFolderStructureInfo() {
        Map<String, String> info = new HashMap<>();
        String mainFolderId = folderCache.get(MAIN_FOLDER_NAME);
        if (mainFolderId != null) {
            info.put("main_folder", MAIN_FOLDER_NAME + " (" + mainFolderId + ")");
            
            String pdfFolderId = folderCache.get(mainFolderId + "/" + PDF_FOLDER_NAME);
            String imageFolderId = folderCache.get(mainFolderId + "/" + IMAGE_FOLDER_NAME);
            String voiceFolderId = folderCache.get(mainFolderId + "/" + VOICE_FOLDER_NAME);
            String docFolderId = folderCache.get(mainFolderId + "/" + DOC_FOLDER_NAME);
            
            info.put("pdf_folder", PDF_FOLDER_NAME + " (" + pdfFolderId + ")");
            info.put("image_folder", IMAGE_FOLDER_NAME + " (" + imageFolderId + ")");
            info.put("voice_folder", VOICE_FOLDER_NAME + " (" + voiceFolderId + ")");
            info.put("doc_folder", DOC_FOLDER_NAME + " (" + docFolderId + ")");
            
            // Debug logging
            android.util.Log.d("DriveServiceHelper", "Folder structure info:");
            android.util.Log.d("DriveServiceHelper", "Main: " + mainFolderId);
            android.util.Log.d("DriveServiceHelper", "PDF: " + pdfFolderId);
            android.util.Log.d("DriveServiceHelper", "Image: " + imageFolderId);
            android.util.Log.d("DriveServiceHelper", "Voice: " + voiceFolderId);
            android.util.Log.d("DriveServiceHelper", "Doc: " + docFolderId);
        }
        return info;
    }

    /**
     * Debug method to list all cached folders
     */
    public void logCachedFolders() {
        android.util.Log.d("DriveServiceHelper", "=== CACHED FOLDERS ===");
        for (Map.Entry<String, String> entry : folderCache.entrySet()) {
            android.util.Log.d("DriveServiceHelper", "Cache Key: " + entry.getKey() + " -> Folder ID: " + entry.getValue());
        }
        android.util.Log.d("DriveServiceHelper", "=====================");
    }

    /**
     * Clears the folder cache and forces re-initialization of folder structure
     */
    public Task<String> resetFolderStructure() {
        android.util.Log.d("DriveServiceHelper", "Clearing folder cache and re-initializing structure");
        folderCache.clear();
        return ensureBaartaFolderStructure();
    }

    /**
     * Debug method to find and log all folders with specific names under Baarta
     */
    public Task<Void> debugFolderStructure() {
        return Tasks.call(mExecutor, () -> {
            try {
                android.util.Log.d("DriveServiceHelper", "=== DEBUGGING FOLDER STRUCTURE ===");
                
                // First find the main Baarta folder
                String mainFolderId = findFolderByName(MAIN_FOLDER_NAME, "root");
                if (mainFolderId == null) {
                    android.util.Log.d("DriveServiceHelper", "No Baarta folder found");
                    return null;
                }
                
                android.util.Log.d("DriveServiceHelper", "Main Baarta folder ID: " + mainFolderId);
                
                // List all subfolders
                String query = "mimeType='application/vnd.google-apps.folder' and '" + mainFolderId + "' in parents and trashed=false";
                FileList result = mDriveService.files().list()
                        .setQ(query)
                        .setFields("files(id, name)")
                        .execute();
                
                android.util.Log.d("DriveServiceHelper", "Found " + result.getFiles().size() + " subfolders:");
                for (File folder : result.getFiles()) {
                    android.util.Log.d("DriveServiceHelper", "  - " + folder.getName() + " (ID: " + folder.getId() + ")");
                }
                
                android.util.Log.d("DriveServiceHelper", "==================================");
                return null;
            } catch (Exception e) {
                android.util.Log.e("DriveServiceHelper", "Error debugging folder structure", e);
                return null;
            }
        });
    }

}

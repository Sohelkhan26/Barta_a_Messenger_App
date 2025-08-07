package com.example.barta_a_messenger_app;

import static com.google.common.io.Files.getFileExtension;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<MessageModel> messageModels;
    Context context;
    String recId;

    private boolean forwardMode = false;
    private ArrayList<MessageModel> selectedMessages = new ArrayList<>();
    private boolean isMultiSelect = false;

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    private boolean isSelectionMode = false;
    private OnMessageSelectListener messageSelectListener;

    // Add interface for callback
    public interface OnMessageSelectListener {

        void onMessageSelectModeActivated();

        void onMessageSelected(ArrayList<MessageModel> messages);
    }

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context) {
        try {
            if (messageModels == null) {
                this.messageModels = new ArrayList<>();
                android.util.Log.w("ChatAdapter", "messageModels was null, initialized empty list");
            } else {
                this.messageModels = messageModels;
            }
            
            if (context == null) {
                android.util.Log.e("ChatAdapter", "Context is null in constructor");
                throw new IllegalArgumentException("Context cannot be null");
            }
            this.context = context;
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error in constructor: " + e.getMessage());
            throw e;
        }
    }

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String recId) {
        try {
            if (messageModels == null) {
                this.messageModels = new ArrayList<>();
                android.util.Log.w("ChatAdapter", "messageModels was null, initialized empty list");
            } else {
                this.messageModels = messageModels;
            }
            
            if (context == null) {
                android.util.Log.e("ChatAdapter", "Context is null in constructor");
                throw new IllegalArgumentException("Context cannot be null");
            }
            this.context = context;
            this.recId = recId;

            if (context instanceof OnMessageSelectListener) {
                this.messageSelectListener = (OnMessageSelectListener) context;
            }
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error in constructor: " + e.getMessage());
            throw e;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            if (parent == null || context == null) {
                android.util.Log.e("ChatAdapter", "Parent or context is null in onCreateViewHolder");
                throw new IllegalStateException("Parent or context cannot be null");
            }
            
            if (viewType == SENDER_VIEW_TYPE) {
                View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
                return new SenderViewHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
                return new ReceiverViewHolder(view);
            }
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error in onCreateViewHolder: " + e.getMessage());
            // Fallback to a default view to prevent crash
            View fallbackView = new View(context);
            return new RecyclerView.ViewHolder(fallbackView) {};
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            // Validate position and messageModels
            if (messageModels == null || position < 0 || position >= messageModels.size()) {
                android.util.Log.w("ChatAdapter", "Invalid position or null messageModels in getItemViewType");
                return RECEIVER_VIEW_TYPE; // Default to receiver type
            }
            
            MessageModel messageModel = messageModels.get(position);
            if (messageModel == null || messageModel.getUid() == null) {
                android.util.Log.w("ChatAdapter", "Null message model or UID in getItemViewType");
                return RECEIVER_VIEW_TYPE; // Default to receiver type
            }
            
            // Check if Firebase user is available
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                android.util.Log.w("ChatAdapter", "No authenticated user in getItemViewType, defaulting to receiver");
                return RECEIVER_VIEW_TYPE; // Default to receiver type when no user
            }
            
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (currentUid == null) {
                android.util.Log.w("ChatAdapter", "Current user UID is null in getItemViewType");
                return RECEIVER_VIEW_TYPE; // Default to receiver type
            }
            
            if (messageModel.getUid().equals(currentUid)) {
                return SENDER_VIEW_TYPE;
            } else {
                return RECEIVER_VIEW_TYPE;
            }
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error in getItemViewType: " + e.getMessage());
            return RECEIVER_VIEW_TYPE; // Safe default
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            // Validate inputs to prevent crashes during authentication changes
            if (holder == null || messageModels == null || position < 0 || position >= messageModels.size()) {
                android.util.Log.w("ChatAdapter", "Invalid parameters in onBindViewHolder");
                return;
            }
            
            MessageModel messageModel = messageModels.get(position);
            if (messageModel == null) {
                android.util.Log.w("ChatAdapter", "Message model is null at position: " + position);
                return;
            }
            
            // Check Firebase auth state before proceeding
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                android.util.Log.w("ChatAdapter", "No authenticated user in onBindViewHolder, skipping bind");
                return;
            }

            if (messageModel.getMessageType() == null || messageModel.getMessageType().equals("msg")) {
                if (holder.getClass() == SenderViewHolder.class) {
                    ((SenderViewHolder) holder).sentImage.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).sentFile.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).senderMsg.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).senderMsg.setText(messageModel.getMessage());
                    ((SenderViewHolder) holder).senderTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));

                    if (messageModel.isGroupMessage()) {
                        ((SenderViewHolder) holder).senderName.setVisibility(View.VISIBLE);
                        ((SenderViewHolder) holder).senderName.setText(messageModel.getSenderName());
                    } else {
                        ((SenderViewHolder) holder).senderName.setVisibility(View.GONE);
                    }
                } else {
                    ((ReceiverViewHolder) holder).receivedImage.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).receivedFile.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder) holder).receiverMsg.setText(messageModel.getMessage());
                    ((ReceiverViewHolder) holder).receiverTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));

                    if (messageModel.isGroupMessage()) {
                        ((ReceiverViewHolder) holder).receiverName.setVisibility(View.VISIBLE);
                        ((ReceiverViewHolder) holder).receiverName.setText(messageModel.getSenderName());
                    } else {
                        ((ReceiverViewHolder) holder).receiverName.setVisibility(View.GONE);
                    }
                }
            } else if (messageModel.getMessageType().equals("img")) {
                if (holder.getClass() == SenderViewHolder.class) {
                    ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).sentFile.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).sentImage.setVisibility(View.VISIBLE);
                    
                    // Decrypt image URL before loading
                    try {
                        String imageUrl = decryptFileUrl(context, messageModel, messageModel.getMessage());
                        if (imageUrl != null && !imageUrl.equals(messageModel.getMessage()) && !imageUrl.isEmpty()) {
                            // Successfully decrypted to a different URL
                            Picasso.get().load(imageUrl).into(((SenderViewHolder) holder).sentImage);
                            android.util.Log.d("ImageDecrypt", "Loaded decrypted image URL");
                        } else {
                            // Either decryption failed or URL was already decrypted, try loading directly
                            String originalUrl = messageModel.getMessage();
                            if (originalUrl != null && !originalUrl.isEmpty()) {
                                Picasso.get().load(originalUrl).into(((SenderViewHolder) holder).sentImage);
                                android.util.Log.d("ImageDecrypt", "Loaded original image URL");
                            } else {
                                android.util.Log.w("ImageDecrypt", "Empty image URL, skipping load");
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ImageDecrypt", "Error loading image: " + e.getMessage());
                        // Try to load original URL as last resort
                        try {
                            String originalUrl = messageModel.getMessage();
                            if (originalUrl != null && !originalUrl.isEmpty()) {
                                Picasso.get().load(originalUrl).into(((SenderViewHolder) holder).sentImage);
                        }
                    } catch (Exception ex) {
                        android.util.Log.e("ImageDecrypt", "Failed to load original image too: " + ex.getMessage());
                    }
                }
                
                ((SenderViewHolder) holder).senderTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));

                if (messageModel.isGroupMessage()) {
                    ((SenderViewHolder) holder).senderName.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).senderName.setText(messageModel.getSenderName());
                } else {
                    ((SenderViewHolder) holder).senderName.setVisibility(View.GONE);
                }
            } else {
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receivedFile.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receivedImage.setVisibility(View.VISIBLE);
                
                // Decrypt image URL before loading
                try {
                    String imageUrl = decryptFileUrl(context, messageModel, messageModel.getMessage());
                    if (imageUrl != null && !imageUrl.equals(messageModel.getMessage()) && !imageUrl.isEmpty()) {
                        // Successfully decrypted to a different URL
                        Picasso.get().load(imageUrl).into(((ReceiverViewHolder) holder).receivedImage);
                        android.util.Log.d("ImageDecrypt", "Loaded decrypted image URL");
                    } else {
                        // Either decryption failed or URL was already decrypted, try loading directly
                        String originalUrl = messageModel.getMessage();
                        if (originalUrl != null && !originalUrl.isEmpty()) {
                            Picasso.get().load(originalUrl).into(((ReceiverViewHolder) holder).receivedImage);
                            android.util.Log.d("ImageDecrypt", "Loaded original image URL");
                        } else {
                            android.util.Log.w("ImageDecrypt", "Empty image URL, skipping load");
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("ImageDecrypt", "Error loading image: " + e.getMessage());
                    // Try to load original URL as last resort
                    try {
                        String originalUrl = messageModel.getMessage();
                        if (originalUrl != null && !originalUrl.isEmpty()) {
                            Picasso.get().load(originalUrl).into(((ReceiverViewHolder) holder).receivedImage);
                        }
                    } catch (Exception ex) {
                        android.util.Log.e("ImageDecrypt", "Failed to load original image too: " + ex.getMessage());
                    }
                }
                
                ((ReceiverViewHolder) holder).receiverTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));

                if (messageModel.isGroupMessage()) {
                    ((ReceiverViewHolder) holder).receiverName.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder) holder).receiverName.setText(messageModel.getSenderName());
                } else {
                    ((ReceiverViewHolder) holder).receiverName.setVisibility(View.GONE);
                }
            }
        } else {
            if (holder.getClass() == SenderViewHolder.class) {
                ((SenderViewHolder) holder).sentFile.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sentImage.setVisibility(View.GONE);

                if (messageModel.getMessageType().equals("voice")) {
                    ((SenderViewHolder) holder).sentFile.setImageResource(R.drawable.ic_voice);
                } else if (messageModel.getMessageType().equals("pdf")) {
                    ((SenderViewHolder) holder).sentFile.setImageResource(R.drawable.pdf_icon);
                } else {
                    ((SenderViewHolder) holder).sentFile.setImageResource(R.drawable.word_icon);
                }
                ((SenderViewHolder) holder).senderTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));

                if (messageModel.isGroupMessage()) {
                    ((SenderViewHolder) holder).senderName.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).senderName.setText(messageModel.getSenderName());
                } else {
                    ((SenderViewHolder) holder).senderName.setVisibility(View.GONE);
                }
            } else {
                ((ReceiverViewHolder) holder).receivedFile.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receivedImage.setVisibility(View.GONE);

                if (messageModel.getMessageType().equals("voice")) {
                    ((ReceiverViewHolder) holder).receivedFile.setImageResource(R.drawable.ic_voice);
                } else if (messageModel.getMessageType().equals("pdf")) {
                    ((ReceiverViewHolder) holder).receivedFile.setImageResource(R.drawable.pdf_icon);
                } else {
                    ((ReceiverViewHolder) holder).receivedFile.setImageResource(R.drawable.word_icon);
                }
                ((ReceiverViewHolder) holder).receiverTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));

                if (messageModel.isGroupMessage()) {
                    ((ReceiverViewHolder) holder).receiverName.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder) holder).receiverName.setText(messageModel.getSenderName());
                } else {
                    ((ReceiverViewHolder) holder).receiverName.setVisibility(View.GONE);
                }
            }
        }

        // Set long click listener on message view
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isSelectionMode = true;
                toggleMessageSelection(position, holder);
                if (messageSelectListener != null) {
                    messageSelectListener.onMessageSelectModeActivated();
                }
                return true;
            }
        });

        // Set normal click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSelectionMode) {
                    toggleMessageSelection(position, holder);
                }
            }
        });

        // Update selection state
        if (selectedMessages.contains(messageModel)) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error in onBindViewHolder: " + e.getMessage());
            // Don't crash the app, just log the error
        }
    }

    private void toggleMessageSelection(MessageModel message, View view) {
        if (selectedMessages.contains(message)) {
            selectedMessages.remove(message);
            view.setBackgroundColor(Color.TRANSPARENT);
        } else {
            selectedMessages.add(message);
            view.setBackgroundColor(Color.LTGRAY);
        }

        if (messageSelectListener != null) {
            if (selectedMessages.isEmpty()) {
                isSelectionMode = false;
            }
            messageSelectListener.onMessageSelected(selectedMessages);
        }
    }

    private void toggleMessageSelection(int position, RecyclerView.ViewHolder holder) {
        MessageModel messageModel = messageModels.get(position);
        toggleMessageSelection(messageModel, holder.itemView);
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMsg, receiverTime, receiverName;
        ImageView receivedImage;
        ImageButton receivedFile;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
            receiverName = itemView.findViewById(R.id.receiverName);
            receivedImage = itemView.findViewById(R.id.received_image);
            receivedFile = itemView.findViewById(R.id.received_file);
            receivedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    int position = getAdapterPosition();
                    MessageModel messageModel = messageModels.get(position);
                    openItem(c, messageModel);
                }
            });
            receivedFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    int position = getAdapterPosition();
                    MessageModel messageModel = messageModels.get(position);
                    openItem(c, messageModel);
                }
            });
        }
    }

    void openItem(Context context, MessageModel messageModel) {
        try {
            String fileUrl = messageModel.getMessage();
            
            // Validate message model and URL
            if (messageModel == null || fileUrl == null || fileUrl.isEmpty()) {
                android.util.Log.w("ChatAdapter", "Invalid message or empty URL");
                Toast.makeText(context, "Cannot open empty file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (messageModel.getMessageType().equals("voice")) {
                // Check if URL is already decrypted (starts with https://)
                if (fileUrl.startsWith("https://")) {
                    android.util.Log.d("VoicePlayback", "URL already decrypted: " + fileUrl);
                    playVoiceMessage(context, fileUrl);
                } else {
                    // Try to decrypt voice message URL using proper encryption key system
                    String decryptedUrl = decryptFileUrl(context, messageModel, fileUrl);
                    if (decryptedUrl != null && !decryptedUrl.isEmpty()) {
                        playVoiceMessage(context, decryptedUrl);
                    } else {
                        // Try to play directly if decryption fails (for backward compatibility)
                        android.util.Log.d("VoicePlayback", "Trying to play URL directly as fallback");
                        playVoiceMessage(context, fileUrl);
                    }
                }
                return;
            }

            // Decrypt file URL for other file types
            String decryptedFileUrl = fileUrl;
            if (messageModel.getMessageType().equals("img")
                    || messageModel.getMessageType().equals("pdf")
                    || messageModel.getMessageType().equals("docx")) {
                decryptedFileUrl = decryptFileUrl(context, messageModel, fileUrl);
                // decryptFileUrl now handles fallbacks internally, so we always get a valid URL
            }

            if (decryptedFileUrl == null || decryptedFileUrl.isEmpty()) {
                android.util.Log.w("ChatAdapter", "Failed to decrypt file URL");
                Toast.makeText(context, "Cannot open encrypted file", Toast.LENGTH_SHORT).show();
                return;
            }
            android.util.Log.d("FileDecrypt", "Final file URL: " + decryptedFileUrl);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(decryptedFileUrl);

            if (messageModel.getMessageType().equals("pdf")) {
                // Set MIME type for PDFs
                intent.setDataAndType(uri, "application/pdf");
            } else if (messageModel.getMessageType().equals("docx")) {
                // Set MIME type for Word documents
                intent.setDataAndType(uri, "application/msword");
            } else if (messageModel.getMessageType().equals("img")) {
                intent.setDataAndType(uri, "image/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error in openItem: " + e.getMessage());
            Toast.makeText(context, "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void playVoiceMessage(Context context, String voiceUrl) {
        try {
            // Validate input parameters
            if (context == null) {
                android.util.Log.e("VoicePlayback", "Context is null");
                return;
            }
            
            if (voiceUrl == null || voiceUrl.isEmpty()) {
                android.util.Log.e("VoicePlayback", "Voice URL is null or empty");
                Toast.makeText(context, "Invalid voice message URL", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // First try to open in browser/external player as fallback
            if (voiceUrl.contains("drive.google.com")) {
                // For Google Drive files, create a direct download link
                String fileId = extractFileIdFromDriveUrl(voiceUrl);
                if (fileId != null && !fileId.isEmpty()) {
                    String directUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
                    playAudioFromUrl(context, directUrl);
                } else {
                    // If we can't extract file ID, try opening in browser
                    Toast.makeText(context, "Opening voice message in browser...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(voiceUrl));
                    context.startActivity(intent);
                }
            } else {
                playAudioFromUrl(context, voiceUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("VoicePlayback", "Exception in playVoiceMessage: " + e.getMessage());
            Toast.makeText(context, "Failed to play voice message. Opening in browser...", Toast.LENGTH_SHORT).show();

            // Fallback: open in browser
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(voiceUrl));
                context.startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(context, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String extractFileIdFromDriveUrl(String url) {
        try {
            // Extract file ID from Google Drive URL
            if (url.contains("/file/d/")) {
                String[] parts = url.split("/file/d/");
                if (parts.length > 1) {
                    String fileIdPart = parts[1];
                    int slashIndex = fileIdPart.indexOf("/");
                    if (slashIndex > 0) {
                        return fileIdPart.substring(0, slashIndex);
                    } else {
                        return fileIdPart;
                    }
                }
            } else if (url.contains("id=")) {
                String[] parts = url.split("id=");
                if (parts.length > 1) {
                    String fileIdPart = parts[1];
                    int ampersandIndex = fileIdPart.indexOf("&");
                    if (ampersandIndex > 0) {
                        return fileIdPart.substring(0, ampersandIndex);
                    } else {
                        return fileIdPart;
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("VoicePlayback", "Error extracting file ID: " + e.getMessage());
        }
        return null;
    }

    private void playAudioFromUrl(Context context, String audioUrl) {
        try {
            // Validate input parameters
            if (context == null) {
                android.util.Log.e("VoicePlayback", "Context is null in playAudioFromUrl");
                return;
            }
            
            if (audioUrl == null || audioUrl.isEmpty()) {
                android.util.Log.e("VoicePlayback", "Audio URL is null or empty in playAudioFromUrl");
                Toast.makeText(context, "Invalid audio URL", Toast.LENGTH_SHORT).show();
                return;
            }
            
            MediaPlayer mediaPlayer = new MediaPlayer();

            // Set audio attributes for better compatibility
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                mediaPlayer.setAudioAttributes(audioAttributes);
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            // Set volume to maximum
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            // Check if volume is too low
            if (currentVolume < maxVolume / 2) {
                Toast.makeText(context, "Volume might be low. Please check your media volume.", Toast.LENGTH_LONG).show();
            }

            android.util.Log.d("VoicePlayback", "Setting data source: " + audioUrl);
            mediaPlayer.setDataSource(audioUrl);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Set volume to maximum for this playback
                    mp.setVolume(1.0f, 1.0f);
                    mp.start();
                    Toast.makeText(context, "Playing voice message... (Duration: " + (mp.getDuration() / 1000) + "s)", Toast.LENGTH_SHORT).show();

                    // Log for debugging
                    android.util.Log.d("VoicePlayback", "Voice message started playing. Duration: " + mp.getDuration() + "ms");
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    android.util.Log.d("VoicePlayback", "Voice message playback completed");
                    mp.release();
                    Toast.makeText(context, "Voice message finished", Toast.LENGTH_SHORT).show();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    android.util.Log.e("VoicePlayback", "MediaPlayer error: what=" + what + ", extra=" + extra);
                    mp.release();

                    String errorMsg = "MediaPlayer failed. Opening in browser...";
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();

                    // Fallback: open in browser
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(audioUrl));
                        context.startActivity(intent);
                    } catch (Exception ex) {
                        Toast.makeText(context, "Browser open failed too: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });

            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    android.util.Log.d("VoicePlayback", "MediaPlayer info: what=" + what + ", extra=" + extra);
                    if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        Toast.makeText(context, "Buffering voice message...", Toast.LENGTH_SHORT).show();
                    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        Toast.makeText(context, "Buffering complete", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            android.util.Log.d("VoicePlayback", "Starting to prepare voice message from URL: " + audioUrl);
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("VoicePlayback", "Exception in playAudioFromUrl: " + e.getMessage());
            Toast.makeText(context, "Failed to play audio. Opening in browser...", Toast.LENGTH_LONG).show();

            // Fallback: open in browser
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(audioUrl));
                context.startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(context, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Decrypts a file URL using the appropriate encryption key based on the message sender
     * @param context The context
     * @param messageModel The message model containing sender information
     * @param encryptedUrl The encrypted URL to decrypt
     * @return The decrypted URL or original URL if decryption fails
     */
    private String decryptFileUrl(Context context, MessageModel messageModel, String encryptedUrl) {
        try {
            // Validate inputs
            if (context == null || messageModel == null || encryptedUrl == null || encryptedUrl.isEmpty()) {
                android.util.Log.w("FileDecrypt", "Invalid input parameters, returning original URL");
                return encryptedUrl != null ? encryptedUrl : "";
            }
            
            android.util.Log.d("FileDecrypt", "Attempting to decrypt URL for message from: " + messageModel.getUid());
            android.util.Log.d("FileDecrypt", "URL preview: " + encryptedUrl.substring(0, Math.min(50, encryptedUrl.length())) + "...");
            
            // Check if URL is already decrypted (starts with https:// or http://)
            if (encryptedUrl.startsWith("https://") || encryptedUrl.startsWith("http://")) {
                android.util.Log.d("FileDecrypt", "URL already decrypted, returning as-is");
                return encryptedUrl;
            }
            
            // Check if the data looks like Base64 (basic validation)
            if (!isValidBase64(encryptedUrl)) {
                android.util.Log.w("FileDecrypt", "Data doesn't appear to be valid Base64, returning as-is");
                return encryptedUrl;
            }
            
            // Check if Firebase user is available
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                android.util.Log.w("FileDecrypt", "No authenticated user, returning original URL");
                return encryptedUrl;
            }
            
            // Get current user ID to determine the friend's ID for decryption
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String friendUid;
            
            // Validate UIDs
            if (currentUserId == null || messageModel.getUid() == null) {
                android.util.Log.w("FileDecrypt", "Invalid user IDs, returning original URL");
                return encryptedUrl;
            }
            
            // Determine which user is the friend (not the current user)
            if (messageModel.getUid().equals(currentUserId)) {
                // Message is from current user, friend is the receiver
                friendUid = recId;
            } else {
                // Message is from the other user, they are the friend
                friendUid = messageModel.getUid();
            }
            
            // Validate friend UID
            if (friendUid == null || friendUid.isEmpty()) {
                android.util.Log.w("FileDecrypt", "Invalid friend UID, returning original URL");
                return encryptedUrl;
            }
            
            EncryptionKeyManager keyManager = null;
            String decryptedUrl = null;
            
            try {
                keyManager = new EncryptionKeyManager(context);
                
                // Try to decrypt with friend-specific key first
                decryptedUrl = keyManager.decryptFromFriend(friendUid, encryptedUrl);
                
                // If friend-specific key not found, use fallback key for compatibility
                if (decryptedUrl == null) {
                    decryptedUrl = CryptoHelper.decryptWithFallbackKey(encryptedUrl);
                    android.util.Log.w("ChatAdapter", "Using fallback decryption for file from: " + friendUid);
                } else {
                    android.util.Log.d("ChatAdapter", "Successfully decrypted file URL using friend-specific key");
                }
                
            } catch (Exception e) {
                android.util.Log.e("FileDecrypt", "Decryption failed: " + e.getMessage());
                // If decryption fails, return the original URL as fallback
                android.util.Log.w("FileDecrypt", "Returning original URL as fallback");
                decryptedUrl = encryptedUrl;
            } finally {
                // Safely close the key manager
                if (keyManager != null) {
                    try {
                        keyManager.close();
                    } catch (Exception e) {
                        android.util.Log.w("FileDecrypt", "Error closing key manager: " + e.getMessage());
                    }
                }
            }
            
            return decryptedUrl != null ? decryptedUrl : encryptedUrl;
            
        } catch (Exception e) {
            android.util.Log.e("FileDecrypt", "Failed to decrypt file URL: " + e.getMessage(), e);
            // Return original URL as fallback for any unexpected errors
            return encryptedUrl != null ? encryptedUrl : "";
        }
    }
    
    /**
     * Basic validation to check if a string looks like Base64
     * @param str The string to check
     * @return true if it looks like Base64, false otherwise
     */
    private boolean isValidBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        // Base64 strings should only contain these characters
        String base64Pattern = "^[A-Za-z0-9+/]*={0,2}$";
        
        // Check basic pattern and length (Base64 length should be multiple of 4)
        return str.matches(base64Pattern) && str.length() % 4 == 0 && str.length() > 20;
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMsg, senderTime, senderName;
        ImageView sentImage;
        ImageButton sentFile;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderName = itemView.findViewById(R.id.senderName);
            sentImage = itemView.findViewById(R.id.sent_image);
            sentFile = itemView.findViewById(R.id.sent_file);

            sentFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    int position = getAdapterPosition();
                    MessageModel messageModel = messageModels.get(position);

                    openItem(c, messageModel);
                }
            });
            sentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    int position = getAdapterPosition();
                    MessageModel messageModel = messageModels.get(position);
                    openItem(c, messageModel);
                }
            });
        }
    }

    public void setForwardMode(boolean forwardMode) {
        this.forwardMode = forwardMode;
        if (!forwardMode) {
            selectedMessages.clear();
        }
    }

    public void clearSelection() {
        isSelectionMode = false;
        selectedMessages.clear();
        notifyDataSetChanged();
    }

    public void setOnMessageSelectListener(OnMessageSelectListener listener) {
        messageSelectListener = listener;
    }
    
    /**
     * Call this method when authentication state changes to refresh the adapter safely
     */
    public void onAuthStateChanged() {
        try {
            android.util.Log.d("ChatAdapter", "Authentication state changed, refreshing adapter");
            // Clear selection mode to prevent issues
            isSelectionMode = false;
            selectedMessages.clear();
            
            // Notify adapter to refresh all views
            notifyDataSetChanged();
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error handling auth state change: " + e.getMessage());
        }
    }
    
    /**
     * Update the receiver ID when account changes
     */
    public void updateReceiverId(String newRecId) {
        try {
            this.recId = newRecId;
            android.util.Log.d("ChatAdapter", "Updated receiver ID to: " + (newRecId != null ? "***" : "null"));
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error updating receiver ID: " + e.getMessage());
        }
    }
    
    /**
     * Check if the adapter is in a valid state for operations
     */
    public boolean isValidState() {
        try {
            return context != null && 
                   messageModels != null && 
                   FirebaseAuth.getInstance().getCurrentUser() != null;
        } catch (Exception e) {
            android.util.Log.e("ChatAdapter", "Error checking adapter state: " + e.getMessage());
            return false;
        }
    }
}

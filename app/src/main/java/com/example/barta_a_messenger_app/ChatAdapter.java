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
        this.messageModels = messageModels;
        this.context = context;
    }

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String recId) {
        this.messageModels = messageModels;
        this.context = context;
        this.recId = recId;

        if (context instanceof OnMessageSelectListener) {
            this.messageSelectListener = (OnMessageSelectListener) context;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = messageModels.get(position);

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
                Picasso.get().load(messageModel.getMessage()).into(((SenderViewHolder) holder).sentImage);
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
                Picasso.get().load(messageModel.getMessage()).into(((ReceiverViewHolder) holder).receivedImage);
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
        String fileUrl = messageModel.getMessage();

        if (messageModel.getMessageType().equals("voice")) {
            // Check if URL is already decrypted (starts with https://)
            if (fileUrl.startsWith("https://")) {
                android.util.Log.d("VoicePlayback", "URL already decrypted: " + fileUrl);
                playVoiceMessage(context, fileUrl);
            } else {
                // Try to decrypt voice message URL
                try {
                    android.util.Log.d("VoicePlayback", "Attempting to decrypt URL: " + fileUrl.substring(0, Math.min(50, fileUrl.length())) + "...");
                    String decryptedUrl = CryptoHelper.decrypt("H@rrY_p0tter_106", fileUrl);
                    android.util.Log.d("VoicePlayback", "Successfully decrypted voice URL: " + decryptedUrl);
                    // Play voice message with decrypted URL
                    playVoiceMessage(context, decryptedUrl);
                } catch (Exception e) {
                    android.util.Log.e("VoicePlayback", "Failed to decrypt voice URL: " + e.getMessage());
                    android.util.Log.e("VoicePlayback", "Original URL length: " + fileUrl.length());

                    // Try to play directly if decryption fails (for backward compatibility)
                    android.util.Log.d("VoicePlayback", "Trying to play URL directly as fallback");
                    playVoiceMessage(context, fileUrl);
                }
            }
            return;
        }

        // Decrypt file URL for other file types
        String decryptedFileUrl = fileUrl;
        try {
            if (messageModel.getMessageType().equals("img")
                    || messageModel.getMessageType().equals("pdf")
                    || messageModel.getMessageType().equals("docx")) {
                decryptedFileUrl = CryptoHelper.decrypt("H@rrY_p0tter_106", fileUrl);
                android.util.Log.d("FileDecrypt", "Decrypted file URL: " + decryptedFileUrl);
            }
        } catch (Exception e) {
            android.util.Log.e("FileDecrypt", "Failed to decrypt file URL: " + e.getMessage());
            Toast.makeText(context, "Failed to decrypt file", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(decryptedFileUrl);

        if (messageModel.getMessageType().equals("pdf")) {
            // Set MIME type for PDFs
            intent.setDataAndType(uri, "application/pdf");
        } else if (messageModel.equals("docx")) {
            // Set MIME type for Word documents
            intent.setDataAndType(uri, "application/msword");
        } else if (messageModel.getMessageType().equals("img")) {
            intent.setDataAndType(uri, "image/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void playVoiceMessage(Context context, String voiceUrl) {
        try {
            // First try to open in browser/external player as fallback
            if (voiceUrl.contains("drive.google.com")) {
                // For Google Drive files, create a direct download link
                String fileId = extractFileIdFromDriveUrl(voiceUrl);
                if (fileId != null) {
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
}

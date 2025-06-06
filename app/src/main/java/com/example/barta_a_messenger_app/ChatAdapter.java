package com.example.barta_a_messenger_app;

import static com.google.common.io.Files.getFileExtension;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
    int DATE_SEPARATOR_VIEW_TYPE = 3;

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
        } else if (viewType == RECEIVER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
            return new ReceiverViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_date_separator, parent, false);
            return new DateSeparatorViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageModels.get(position);

        // Check if this message needs a date separator
        if (position == 0 || !isSameDay(messageModels.get(position - 1).getTimestamp(), message.getTimestamp())) {
            if (message.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                return SENDER_VIEW_TYPE;
            } else {
                return RECEIVER_VIEW_TYPE;
            }
        }

        if (message.getUid().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    private boolean isSameDay(long timestamp1, long timestamp2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date(timestamp1)).equals(sdf.format(new Date(timestamp2)));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        MessageModel messageModel = messageModels.get(position);

        // Check if we need to show date separator
        boolean showDateSeparator = position == 0 || !isSameDay(messageModels.get(position - 1).getTimestamp(), messageModel.getTimestamp());

        if (holder instanceof SenderViewHolder) {
            SenderViewHolder senderHolder = (SenderViewHolder) holder;

            // Show date separator if needed
            if (showDateSeparator) {
                senderHolder.dateSeparator.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
                senderHolder.dateSeparator.setText(sdf.format(new Date(messageModel.getTimestamp())));
            } else {
                senderHolder.dateSeparator.setVisibility(View.GONE);
            }

            // Show message content
            if (messageModel.getMessageType() == null || messageModel.getMessageType().equals("msg")) {
                senderHolder.sentImage.setVisibility(View.GONE);
                senderHolder.sentFile.setVisibility(View.GONE);
                senderHolder.senderMsg.setVisibility(View.VISIBLE);
                senderHolder.senderMsg.setText(messageModel.getMessage());
                senderHolder.senderTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));
            } else if (messageModel.getMessageType().equals("img")) {
                senderHolder.senderMsg.setVisibility(View.GONE);
                senderHolder.sentFile.setVisibility(View.GONE);
                senderHolder.sentImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messageModel.getMessage()).into(senderHolder.sentImage);
                senderHolder.senderTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));
            } else {
                senderHolder.sentFile.setVisibility(View.VISIBLE);
                senderHolder.senderMsg.setVisibility(View.GONE);
                senderHolder.sentImage.setVisibility(View.GONE);
                if (messageModel.getMessageType().equals("pdf")) {
                    senderHolder.sentFile.setImageResource(R.drawable.pdf_icon);
                } else {
                    senderHolder.sentFile.setImageResource(R.drawable.word_icon);
                }
                senderHolder.senderTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));
            }

            if (messageModel.isGroupMessage()) {
                senderHolder.senderName.setVisibility(View.VISIBLE);
                senderHolder.senderName.setText(messageModel.getSenderName());
            } else {
                senderHolder.senderName.setVisibility(View.GONE);
            }
        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder receiverHolder = (ReceiverViewHolder) holder;

            // Show date separator if needed
            if (showDateSeparator) {
                receiverHolder.dateSeparator.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
                receiverHolder.dateSeparator.setText(sdf.format(new Date(messageModel.getTimestamp())));
            } else {
                receiverHolder.dateSeparator.setVisibility(View.GONE);
            }

            // Show message content
            if (messageModel.getMessageType() == null || messageModel.getMessageType().equals("msg")) {
                receiverHolder.receivedImage.setVisibility(View.GONE);
                receiverHolder.receivedFile.setVisibility(View.GONE);
                receiverHolder.receiverMsg.setVisibility(View.VISIBLE);
                receiverHolder.receiverMsg.setText(messageModel.getMessage());
                receiverHolder.receiverTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));
            } else if (messageModel.getMessageType().equals("img")) {
                receiverHolder.receiverMsg.setVisibility(View.GONE);
                receiverHolder.receivedFile.setVisibility(View.GONE);
                receiverHolder.receivedImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messageModel.getMessage()).into(receiverHolder.receivedImage);
                receiverHolder.receiverTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));
            } else {
                receiverHolder.receivedFile.setVisibility(View.VISIBLE);
                receiverHolder.receiverMsg.setVisibility(View.GONE);
                receiverHolder.receivedImage.setVisibility(View.GONE);
                if (messageModel.getMessageType().equals("pdf")) {
                    receiverHolder.receivedFile.setImageResource(R.drawable.pdf_icon);
                } else {
                    receiverHolder.receivedFile.setImageResource(R.drawable.word_icon);
                }
                receiverHolder.receiverTime.setText(new SimpleDateFormat("HH:mm a").format(new Date(messageModel.getTimestamp())));
            }

            if (messageModel.isGroupMessage()) {
                receiverHolder.receiverName.setVisibility(View.VISIBLE);
                receiverHolder.receiverName.setText(messageModel.getSenderName());
            } else {
                receiverHolder.receiverName.setVisibility(View.GONE);
            }
        }

        // Set long click listener on message view
        holder.itemView.setOnLongClickListener(v -> {
            Log.d("ChatAdapter", "Message long pressed: " + messageModel.getMessageId());
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (messageSelectListener != null) {
                messageSelectListener.onMessageSelectModeActivated();

                // If message is from current user, allow selection
                if (messageModel.getUid().equals(currentUserId)) {
                    toggleMessageSelection(position, holder);
                } else {
                    // For other's messages, only select this message for forwarding
                    selectedMessages.clear();
                    selectedMessages.add(messageModel);
                    holder.itemView.setBackgroundColor(Color.LTGRAY);
                    messageSelectListener.onMessageSelected(selectedMessages);
                }
            }
            return true;
        });

        // Set normal click listener
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleMessageSelection(position, holder);
            }
        });

        // Update selection state
        if (selectedMessages.contains(messageModel)) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void toggleMessageSelection(int position, RecyclerView.ViewHolder holder) {
        MessageModel messageModel = messageModels.get(position);
        if (selectedMessages.contains(messageModel)) {
            selectedMessages.remove(messageModel);
            Log.d("ChatAdapter", "Message deselected: " + messageModel.getMessageId());
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            selectedMessages.add(messageModel);
            Log.d("ChatAdapter", "Message selected: " + messageModel.getMessageId());
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }

        if (messageSelectListener != null) {
            if (selectedMessages.isEmpty()) {
                isSelectionMode = false;
            }
            messageSelectListener.onMessageSelected(selectedMessages);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMsg, receiverTime, receiverName, dateSeparator;
        ImageView receivedImage;
        ImageButton receivedFile;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
            receiverName = itemView.findViewById(R.id.receiverName);
            dateSeparator = itemView.findViewById(R.id.dateSeparator);
            receivedImage = itemView.findViewById(R.id.received_image);
            receivedFile = itemView.findViewById(R.id.received_file);
            receivedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    int position = getBindingAdapterPosition();
                    MessageModel messageModel = messageModels.get(position);
                    openItem(c, messageModel);
                }
            });
            receivedFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    int position = getBindingAdapterPosition();
                    MessageModel messageModel = messageModels.get(position);
                    openItem(c, messageModel);
                }
            });
        }
    }

    void openItem(Context context, MessageModel messageModel) {
        String fileUrl = messageModel.getMessage();
//        String fileExtension = getFileExtension(fileUrl); // Function to get file extension

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(fileUrl);

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

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMsg, senderTime, senderName, dateSeparator;
        ImageView sentImage;
        ImageButton sentFile;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderName = itemView.findViewById(R.id.senderName);
            dateSeparator = itemView.findViewById(R.id.dateSeparator);
            sentImage = itemView.findViewById(R.id.sent_image);
            sentFile = itemView.findViewById(R.id.sent_file);

            sentFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    int position = getBindingAdapterPosition();
                    MessageModel messageModel = messageModels.get(position);
                    openItem(c, messageModel);
                }
            });
            sentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    int position = getBindingAdapterPosition();
                    MessageModel messageModel = messageModels.get(position);
                    openItem(c, messageModel);
                }
            });
        }
    }

    public class DateSeparatorViewHolder extends RecyclerView.ViewHolder {

        TextView dateText;

        public DateSeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateSeparator);
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

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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
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
                if (messageModel.getMessageType().equals("pdf")) {
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
                if (messageModel.getMessageType().equals("pdf")) {
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

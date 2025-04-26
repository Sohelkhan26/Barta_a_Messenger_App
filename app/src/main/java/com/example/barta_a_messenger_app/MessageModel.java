package com.example.barta_a_messenger_app;

public class MessageModel {

    String uid, message, messageId;
    String messageType;
    String isNotified;
    Long timestamp;
    String senderName;
    boolean isGroupMessage;
    String senderId;

    public MessageModel(String uid, String message, Long timestamp) {
        this.uid = uid;
        this.message = message;
        this.timestamp = timestamp;
        this.messageType = "msg";
        this.isGroupMessage = false;
        this.senderId = uid;
    }

    public MessageModel(String uid, String message, String messageType) {
        this.uid = uid;
        this.message = message;
        this.messageType = messageType;
        this.isGroupMessage = false;
        this.senderId = uid;
    }

    public MessageModel(String uid, String message) {
        this.uid = uid;
        this.message = message;
        this.messageType = "msg";
        this.isGroupMessage = false;
        this.senderId = uid;
    }

    public MessageModel(String messageId, String uid, String message, String messageType, Long timestamp, String isNotified) {
        this.messageId = messageId;
        this.uid = uid;
        this.message = message;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.isNotified = isNotified;
        this.isGroupMessage = false;
        this.senderId = uid;
    }

    public MessageModel() {
        this.isGroupMessage = false;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
        this.senderId = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getIsNotified() {
        return isNotified;
    }

    public void setIsNotified(String isNotified) {
        this.isNotified = isNotified;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public boolean isGroupMessage() {
        return isGroupMessage;
    }

    public void setGroupMessage(boolean groupMessage) {
        isGroupMessage = groupMessage;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}

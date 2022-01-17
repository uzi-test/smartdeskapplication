package com.smartdesk.model.fcm;

public class Data {

    String documentId;
    Long id;
    String messageType;
    String title;
    String description;

    public Data() {
    }

    public Data(String documentId, Long id, String messageType, String title, String description) {
        this.documentId = documentId;
        this.id = id;
        this.messageType = messageType;
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

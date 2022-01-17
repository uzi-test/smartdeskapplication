package com.smartdesk.model.notification;

import java.util.Date;

public class NotificationLocalDTO {

    Integer role;
    String documentID;

    Date date;
    String title;
    String description;
    Boolean isRead;
    Boolean isOpen;
    String localDocumentID;


    public NotificationLocalDTO() {
    }

    public NotificationLocalDTO(Integer role, String documentID, Date date, String title, String description, Boolean isRead, Boolean isOpen) {
        this.role = role;
        this.documentID = documentID;
        this.date = date;
        this.title = title;
        this.description = description;
        this.isRead = isRead;
        this.isOpen = isOpen;
    }

    public String getLocalDocumentID() {
        return localDocumentID;
    }

    public void setLocalDocumentID(String localDocumentID) {
        this.localDocumentID = localDocumentID;
    }

    public Boolean getOpen() {
        return isOpen;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

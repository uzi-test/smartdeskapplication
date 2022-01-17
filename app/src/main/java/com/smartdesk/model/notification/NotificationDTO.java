package com.smartdesk.model.notification;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationDTO implements Comparable<NotificationDTO>{

    Integer role;
    String documentID;

    @ServerTimestamp
    Date date;

    String title;
    String description;
    Boolean isRead;


    public NotificationDTO() {
    }

    public NotificationDTO(Integer role, String documentID, Date date, String title, String description, Boolean isRead) {
        this.role = role;
        this.documentID = documentID;
        this.date = date;
        this.title = title;
        this.description = description;
        this.isRead = isRead;
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

    @Override
    public int compareTo(NotificationDTO o) {
        try {
            return o.getDate().compareTo(this.getDate());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}

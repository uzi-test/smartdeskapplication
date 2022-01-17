package com.smartdesk.model.SmartDesk;

import com.google.firebase.firestore.DocumentReference;

public class UserBookDate {

    public String deskDocId;
    public String userDocId;

    public String date;



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDeskDocId() {
        return deskDocId;
    }

    public void setDeskDocId(String deskDocId) {
        this.deskDocId = deskDocId;
    }

    public String getUserDocId() {
        return userDocId;
    }

    public void setUserDocId(String userDocId) {
        this.userDocId = userDocId;
    }
}

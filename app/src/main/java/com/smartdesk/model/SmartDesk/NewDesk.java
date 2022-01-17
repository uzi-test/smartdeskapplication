package com.smartdesk.model.SmartDesk;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.smartdesk.constants.Constants.const_lat;
import static com.smartdesk.constants.Constants.const_lng;

public class NewDesk {

    public String docID;

    public String id;
    public String name;

    public String wirelessCharging;
    public String builtinSpeaker;
    public String bluetoothConnection;
    public String groupUser;

    public Double deskLat;
    public Double deskLng;

    public List<UserBookDate> bookDate;

    @ServerTimestamp
    public Date registrationDate;


    public NewDesk() {
        docID = "";
        id = "";
        name = "Smart Desk";
        wirelessCharging = "";
        builtinSpeaker = "";
        bluetoothConnection = "";
        groupUser = "";
        bookDate = new ArrayList<>();
        registrationDate = new Timestamp(new Date().getTime());
        deskLat = const_lat;
        deskLng = const_lng;
    }


    public Double getDeskLat() {
        return deskLat;
    }

    public void setDeskLat(Double deskLat) {
        this.deskLat = deskLat;
    }

    public Double getDeskLng() {
        return deskLng;
    }

    public void setDeskLng(Double deskLng) {
        this.deskLng = deskLng;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWirelessCharging() {
        return wirelessCharging;
    }

    public void setWirelessCharging(String wirelessCharging) {
        this.wirelessCharging = wirelessCharging;
    }

    public String getBuiltinSpeaker() {
        return builtinSpeaker;
    }

    public void setBuiltinSpeaker(String builtinSpeaker) {
        this.builtinSpeaker = builtinSpeaker;
    }

    public String getBluetoothConnection() {
        return bluetoothConnection;
    }

    public void setBluetoothConnection(String bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public String getGroupUser() {
        return groupUser;
    }

    public void setGroupUser(String groupUser) {
        this.groupUser = groupUser;
    }

    public List<UserBookDate> getBookDate() {
        return bookDate;
    }

    public void setBookDate(List<UserBookDate> bookDate) {
        this.bookDate = bookDate;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
}

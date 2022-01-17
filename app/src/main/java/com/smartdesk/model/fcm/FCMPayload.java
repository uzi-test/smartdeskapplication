package com.smartdesk.model.fcm;

public class FCMPayload {
    String to;
    Data data;

    public FCMPayload() {
    }

    public FCMPayload(String to, Data data) {
        this.to ="/topics/" +  to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = "/topics/" + to;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}

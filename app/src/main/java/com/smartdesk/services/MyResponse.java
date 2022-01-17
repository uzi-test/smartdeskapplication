package com.smartdesk.services;


public class MyResponse {
    int multicast_id;
    int success;
    int failure;

    public int getMulticast_id() {
        return multicast_id;
    }

    public void setMulticast_id(int multicast_id) {
        this.multicast_id = multicast_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }
}
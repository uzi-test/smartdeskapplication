package com.smartdesk.services;

import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.model.fcm.FCMPayload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Service {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=" + FirebaseConstants.FirebaseKey
    })
    @POST("fcm/send")
    Call<MyResponse> fcmSend(@Body FCMPayload fcmPayload);
}

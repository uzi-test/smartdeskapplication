package com.smartdesk.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.smartdesk.constants.Constants;
import com.smartdesk.screens._splash.ScreenSplash;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.smartdesk.utility.UtilityFunctions.showNotification;


public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("ServicesFCM","Firebase Service");
        System.out.println("In Firebase Service");
        if (remoteMessage.getData()!=null && remoteMessage.getData().size() > 0) {
            System.out.println("Sent recieve Data NOtification");
            final SharedPreferences prefs = getSharedPreferences("info", Context.MODE_PRIVATE);
            final String spDocumentId = prefs.getString(Constants.SP_DOCUMENT_ID, "");
            String documentId = remoteMessage.getData().get("documentId").toString();
            if(documentId.equals(spDocumentId)) {
                System.out.println("Sent NOtification");
                String id = remoteMessage.getData().get("id").toString();
                String messageType = remoteMessage.getData().get("messageType").toString();
                String title = remoteMessage.getData().get("title").toString();
                String description = remoteMessage.getData().get("description").toString();
                Long id1 = Long.parseLong(id);
                showNotification(getApplicationContext(), id1.intValue(), title, description, new Intent(getApplicationContext(), ScreenSplash.class));
            }
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}

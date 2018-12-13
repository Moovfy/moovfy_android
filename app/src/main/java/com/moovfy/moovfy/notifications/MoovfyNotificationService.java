package com.moovfy.moovfy.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class MoovfyNotificationService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("FIREBASE MESSAGING", "Refreshed token: " + token);

    }
}

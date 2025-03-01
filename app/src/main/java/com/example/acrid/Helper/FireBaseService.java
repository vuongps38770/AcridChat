package com.example.acrid.Helper;

import android.Manifest;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.acrid.Constant.DB;
import com.example.acrid.R;
import com.example.acrid.View.Activity.MainActivity;

import com.example.acrid.services.ChatHeadService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.Firebase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Scanner;

public class FireBaseService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String CHANNEL_ID = "AcridChannel";
    public static final String NOTIFICATION_NAME = "Nhận thông báo tin nhắn";
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(FireBaseService.class);

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null&&!remoteMessage.getData().isEmpty()) {
            String senderName = remoteMessage.getData().get("senderName");
            String messageType = remoteMessage.getData().get("messageType");
            String chatId = remoteMessage.getData().get("chatId");
            Log.e("onMessageReceived: ",senderName+"   "+messageType+"   "+chatId );
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(),senderName,messageType);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    private void showNotification(String title, String message, String senderName, String messageType) {
        NotificationManager notificationManager =  getSystemService(NotificationManager.class);
        NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
        String titlefinal = "Tin nhắn từ "+senderName;
        String bodyFinal = messageType.equals(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.TEXT.toString())
                ?message:"Đã gửi 1 ảnh";


        if (existingChannel == null) {
            Log.d(TAG, "Creating new notification channel: " + CHANNEL_ID);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    NOTIFICATION_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Nhận thông báo tin nhắn từ bạn bè");
            notificationManager.createNotificationChannel(channel);
        } else {
            Log.d(TAG, "Notification channel already exists: " + existingChannel.getName());
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(titlefinal)
                .setContentText(bodyFinal)
                .setSmallIcon(R.drawable.binance)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
//        Intent serviceIntent = new Intent(this, ChatHeadService.class);
//        startService(serviceIntent);


        notificationManager.notify(0, notification);
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance(this).beginWith(work).enqueue();
        // [END dispatch_job]
    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        UserRepo.saveToken(UserRepo.getCurrentUserUID());
    }

    public static class MyWorker extends Worker {

        public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            // TODO(developer): add long running task here.
            return Result.success();
        }
    }




}

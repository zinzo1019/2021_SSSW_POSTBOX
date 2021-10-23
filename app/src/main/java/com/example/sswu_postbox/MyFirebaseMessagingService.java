package com.example.sswu_postbox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    // FCM notification 을 사용하기 위한 class
    String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull @NotNull String token) {
        Log.d(TAG, "new token" + token);
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "메시지를 성공적으로 수신했습니다." + " " + remoteMessage.getData().get("title"));
            sendNotification(remoteMessage);
        }
        else {
            Log.d(TAG, "메시지를 수신하지 못했습니다.");
        }
    }

    void sendNotification(RemoteMessage remoteMessage) {
        int uniId = (int) System.currentTimeMillis() / 7;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // channel 구분할 필요가 없어 하나로 통일
        String channelId = "my_channel";
        CharSequence channelName = "my channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT);

        // 상단바 알림의 모양 결정
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setSmallIcon(R.drawable.suryong_icon)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);


        notificationManager.notify(uniId, notificationBuilder.build());
    }
}

package org.linphone.firebase;

/*
FirebaseMessaging.java
Copyright (C) 2017-2019 Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import static android.content.Intent.ACTION_MAIN;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import org.linphone.BuildConfig;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneService;
import org.linphone.R;
import org.linphone.notifications.RegistrationIntentService;
import org.linphone.settings.LinphonePreferences;
import org.linphone.utils.LinphoneUtils;

public class FirebaseMessaging extends FirebaseMessagingService {

    public FirebaseMessaging() {}

    @Override
    public void onNewToken(final String token) {
        // [Notificatore] send new token to Notificatore app.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

        android.util.Log.i("FirebaseIdService", "[Push Notification] Refreshed token: " + token);

        LinphoneUtils.dispatchOnUIThread(
                new Runnable() {
                    @Override
                    public void run() {
                        LinphonePreferences.instance().setPushNotificationRegistrationID(token);
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        SimpleDateFormat formatter =
                new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
        String dateString = formatter.format(new Date(remoteMessage.getSentTime()));

        if (BuildConfig.DEBUG) {
            sendNotification("sentTime: " + dateString); // Only for testing.
        }

        android.util.Log.i(
                "FirebaseMessaging",
                "[Push Notification] MessageType: " + remoteMessage.getMessageType());
        android.util.Log.i("FirebaseMessaging", "[Push Notification] SentTime: " + dateString);
        android.util.Log.i("FirebaseMessaging", "[Push Notification] Received");

        if (!LinphoneService.isReady()) {
            try {
                android.util.Log.i("FirebaseMessaging", "[Push Notification] Starting Service");
                Intent intent = new Intent(ACTION_MAIN);
                intent.setClass(this, LinphoneService.class);
                intent.putExtra("PushNotification", true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    android.util.Log.i(
                            "FirebaseMessaging",
                            "[Push Notification] Starting Service with startForegroundService()");

                    startForegroundService(intent);
                } else {
                    android.util.Log.i(
                            "FirebaseMessaging",
                            "[Push Notification] Starting Service with startService()");

                    startService(intent);
                }

            } catch (Exception e) {
                android.util.Log.i(
                        "FirebaseMessaging", "[Push Notification] Exception: " + e.getMessage());
            }
        }
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, LinphoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "default";
        String channelName = "default";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.nethesis_icon)
                        .setContentTitle("Chiamata in arrivo")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        UUID guid = UUID.randomUUID();
        if (notificationManager != null) {
            notificationManager.notify(
                    guid.toString(), 0 /* ID of notification */, notificationBuilder.build());
        }
    }
}

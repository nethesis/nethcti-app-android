package org.linphone.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import org.linphone.R;

/** This class contains methods for record user identifier and send it to server. */
public class FCMNotification {
    private static String TAG = "FCMNotification";

    /**
     * Send registration id.
     *
     * @param context the context
     * @param user the user
     */
    @SuppressLint("HardwareIds")
    public static void sendRegistrationId(Context context, String user) {
        String deviceId =
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        String lang = Locale.getDefault().getLanguage();
        if (!lang.equals("it")) {
            lang = "en";
        }

        doSendRegistrationId(
                deviceId,
                user,
                context.getString(R.string.notificatore_uri),
                context.getString(R.string.notificatore_app_key),
                lang);
    }

    /**
     * Update registration info.
     *
     * @param context the context
     * @param userId the userId
     */
    public static void updateRegistrationInfo(Context context, String userId) {
        String regId = FirebaseInstanceId.getInstance().getToken();

        if ((regId != null && !regId.equals(""))) {
            // User logged successfully
            Log.d(TAG, "fcm updateRegistrationInfo " + userId + " - " + regId);

            Intent intent = new Intent(context, RegistrationIntentService.class);
            context.startService(intent);
        }
    }

    public static String getNotificatoreUserIdentifier(String netUsername, String domain) {
        return netUsername + "@" + domain;
    }

    private static boolean doSendRegistrationId(
            String deviceId,
            String user,
            String notificatoreUrl,
            String notificatoreAppKey,
            String language) {
        try {
            String urlstring = notificatoreUrl;
            urlstring += "?CMD=initapp&os=2";
            urlstring += "&devtoken=" + deviceId;
            urlstring += "&regid=" + FirebaseInstanceId.getInstance().getToken();
            urlstring += "&appkey=" + notificatoreAppKey;
            if (!TextUtils.isEmpty(language)) urlstring += "&lang=" + language;

            if (user != null && !user.equals("")) urlstring += "&user=" + user;

            String result = getUrlContents(urlstring);

            return result.contains("OK");
        } catch (Exception ex) {
            Log.e(TAG, "FCM Exception", ex);
            return false;
        }
    }

    private static String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(theUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            bufferedReader.close();
            urlConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content.toString();
    }
}

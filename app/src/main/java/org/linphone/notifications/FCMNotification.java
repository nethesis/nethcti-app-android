package org.linphone.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.GsonBuilder;

import org.linphone.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import it.nethesis.models.notificatore.RegisterToken;
import it.nethesis.models.notificatore.RegisterTokenReponse;
import it.nethesis.webservices.NotificatoreRestAPI;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(installationTokenResult -> {
            String regId = installationTokenResult;
            if (!regId.equals("")) {
                // User logged successfully
                Log.d(TAG, "fcm updateRegistrationInfo " + userId + " - " + regId);
                Intent intent = new Intent(context, RegistrationIntentService.class);
                context.startService(intent);
            }
        });
    }

    public static String getNotificatoreUserIdentifier(String netUsername, String domain) {
        return netUsername + "@" + domain;
    }

    private static void doSendRegistrationId(String deviceId, String user, String notificatoreUrl, String notificatoreAppKey, String language) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(installationTokenResult -> {
                String regId = installationTokenResult;
                if (!regId.equals("")) {
                    OkHttpClient.Builder client = new OkHttpClient.Builder();
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    client.addInterceptor(loggingInterceptor);
                    if (notificatoreUrl == null || notificatoreUrl.isEmpty()) return;
                    Retrofit retrofit =
                            new Retrofit.Builder()
                                    .baseUrl(notificatoreUrl)
                                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().serializeNulls().create()))
                                    .client(client.build())
                                    .build();

                    NotificatoreRestAPI restAPIClass = retrofit.create(NotificatoreRestAPI.class);

                    RegisterToken registerData = new RegisterToken();
                    registerData.setRegID(regId);
                    registerData.setOs("2");
                    registerData.setUser(user);
                    registerData.setLanguage(language);
                    registerData.setDevToken(deviceId);

                    Call<RegisterTokenReponse> call =
                            restAPIClass.registerToken(notificatoreAppKey, registerData);
                    call.enqueue(
                            new Callback<RegisterTokenReponse>() {
                                @Override
                                public void onResponse(
                                        Call<RegisterTokenReponse> call,
                                        Response<RegisterTokenReponse> response) {
                                    Log.i(TAG, "FCM Token registered");
                                }

                                @Override
                                public void onFailure(
                                        Call<RegisterTokenReponse> call, Throwable exception) {
                                    Log.e(TAG, "FCM Error registering Token");
                                }
                            });
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "FCM Exception", ex);
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

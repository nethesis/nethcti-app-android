package org.linphone.firebase;

/*
FirebasePushHelper.java
Copyright (C) 2019 Belledonne Communications, Grenoble, France

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

import static it.nethesis.webservices.RetrofitGenerator.calculateSHA256;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.GsonBuilder;

import org.linphone.LinphoneManager;
import org.linphone.NethCTIApplication;
import org.linphone.R;
import org.linphone.settings.LinphonePreferences;
import org.linphone.utils.PushNotificationUtils;
import org.linphone.utils.SharedPreferencesManager;

import it.nethesis.models.notificatore.RegisterFPPToken;
import it.nethesis.webservices.NotificatoreRestAPI;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Keep
public class FirebasePushHelper implements PushNotificationUtils.PushHelperInterface {

    private static String TAG = "FirebasePushHelper";
    private Context context;

    @Override
    public void init(Context context) {
        this.context = context;
        android.util.Log.i(TAG,
                "[Push Notification] firebase push sender id "
                        + context.getString(R.string.gcm_defaultSenderId));
        try {
            FirebaseInstallations.getInstance().getId()
                    .addOnCompleteListener(
                            task -> {
                                if (!task.isSuccessful()) {
                                    android.util.Log.e(TAG,
                                            "[Push Notification] firebase getInstanceId failed: "
                                                    + task.getException());
                                    return;
                                }
                                String token = task.getResult();
                                android.util.Log.i(TAG, "[Push Notification] firebase token is: " + token);
                                LinphonePreferences.instance().setPushNotificationRegistrationID(token);
                            });

        } catch (Exception e) {
            android.util.Log.e(TAG, "Firebase not available.");
        }

        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            android.util.Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        android.util.Log.i(TAG, "FCM registration token is: " + token);
                        SharedPreferencesManager.setFcmToken(context, token);
                    });

        } catch (Exception e) {
            android.util.Log.e(TAG, "FCM not available.");
        }
    }

    @Override
    public void checkForFCMTopics(Context context) {
        // Subscribe to app topics
        boolean isUserLogged = NethCTIApplication.Companion.getInstance().isLogged();
        if (isUserLogged) subscribeToAppTopics(context);
    }

    @Override
    public boolean isAvailable(Context context) {
        GoogleApiAvailabilityLight googleApiAvailability = GoogleApiAvailabilityLight.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void deregister(Context context) {
        unsubscribeFromAppTopics(context);
    }

    private static void subscribeToAppTopics(Context context) {
        String authToken = SharedPreferencesManager.getAuthtoken(LinphoneManager.getInstance().getContext());
        String sha256Topic = calculateSHA256(authToken.substring(authToken.lastIndexOf(':') + 1));
        if(authToken != null) doFPPRegister(
                context,
                context.getString(R.string.notificatore_fpp_uri),
                context.getString(R.string.notificatore_fpp_key),
                sha256Topic,
                false
        );
//        subscribeToFCMTopic(context, sha256Topic);
    }

    private static void unsubscribeFromAppTopics(Context context) {
        String authToken = SharedPreferencesManager.getAuthtoken(LinphoneManager.getInstance().getContext());
        String sha256Topic = calculateSHA256(authToken.substring(authToken.lastIndexOf(':') + 1));
        if(authToken != null) doFPPRegister(
                context,
                context.getString(R.string.notificatore_fpp_uri),
                context.getString(R.string.notificatore_fpp_key),
                sha256Topic,
                true
        );
//        unsubscribeFromFCMTopic(sha256Topic);
    }

    private static void doFPPRegister(Context context, String notificatoreUrl, String notificatoreInstanceToken, String topic, boolean deRegister) {
        try {
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

            String fcmToken = SharedPreferencesManager.getFcmToken(context);

            RegisterFPPToken registerData = new RegisterFPPToken();
            registerData.setToken(fcmToken);
            registerData.setTopic(topic);
            registerData.setType("firebase");

            Call<ResponseBody> call = deRegister ? restAPIClass.deregisterFPPToken(notificatoreInstanceToken, registerData) : restAPIClass.registerFPPToken(notificatoreInstanceToken, registerData);
            call.enqueue(
                    new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                if(deRegister) Log.i(TAG, "FPP Token deregistered"); else Log.i(TAG, "FPP Token registered");
                            } else {
                                if(deRegister) Log.e(TAG, "FPP Error deregistering Token"); else Log.e(TAG, "FPP Error registering Token");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable exception) {
                            if(deRegister) Log.e(TAG, "FPP Error deregistering Token -> " + exception); else Log.e(TAG, "FPP Error registering Token -> " + exception);
                        }
                    });
        } catch (Exception ex) {
            Log.e(TAG, "FPP Exception", ex);
        }
    }

    private static void subscribeToFCMTopic(Context context, String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                android.util.Log.e(TAG,
                        "FCM Subscribe failed: "
                                + task.getException());
                return;
            }
            String msg = "Subscribed to " + topic;
            if (!task.isSuccessful()) {
                msg = "Subscribe failed to " + topic;
            }
            android.util.Log.d(TAG, msg);
        });
    }
    private static void unsubscribeFromFCMTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                android.util.Log.e(TAG,
                        "FCM Unsubscribe failed: "
                                + task.getException());
                return;
            }
            String msg = "Unsubscribed to " + topic;
            if (!task.isSuccessful()) {
                msg = "Unsubscribe failed to " + topic;
            }
            android.util.Log.d(TAG, msg);
        });
    }

}

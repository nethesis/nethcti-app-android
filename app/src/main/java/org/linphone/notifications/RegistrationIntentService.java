package org.linphone.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;
import org.linphone.LinphoneManager;
import org.linphone.core.Address;
import org.linphone.core.ProxyConfig;
import org.linphone.utils.SharedPreferencesManager;

/** Class used to start the Service for user registration. */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    public static final String FOREGROUND_EXTRA = "RegIntentServiceStartForeground";

    /** Instantiates a new Registration intent service. */
    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean foreground = intent.getBooleanExtra(FOREGROUND_EXTRA, false);
        if(foreground) {
            startForeground(1, new Notification()); //TODO: create a proper notification
        }


        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                ProxyConfig proxy = LinphoneManager.getLc().getDefaultProxyConfig();
                String username = SharedPreferencesManager.getUsername(getApplicationContext());

                if (proxy != null && username != null) {
                    Address userAddress = proxy.getIdentityAddress();

                    // [Notificatore] send registration to Notificatore app.
                    FCMNotification.sendRegistrationId(
                            this,
                            FCMNotification.getNotificatoreUserIdentifier(
                                    username, userAddress.getDomain())); // [START register_for_gcm]
                } else {
                    FCMNotification.sendRegistrationId(this, "");
                }
            }
        } catch (Exception e) {
            // If an exception happens while fetching the new token or updating our registration
            // data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Log.e(TAG, "Failed to complete token refresh", e);
        }
    }
}

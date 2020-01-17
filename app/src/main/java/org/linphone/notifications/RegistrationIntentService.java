package org.linphone.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import org.linphone.LinphoneManager;
import org.linphone.core.Address;
import org.linphone.core.ProxyConfig;

/** Class used to start the Service for user registration. */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    /** Instantiates a new Registration intent service. */
    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // TODO : recuperare username per notificatore
                ProxyConfig proxy = LinphoneManager.getLc().getDefaultProxyConfig();
                Address userAddress = proxy.getIdentityAddress();
                String username =
                        FCMNotification.getNotificatoreUserIdentifier(
                                userAddress.getUsername(), userAddress.getDomain());
                FCMNotification.sendRegistrationId(this, username); // [START register_for_gcm]
            }
        } catch (Exception e) {
            // If an exception happens while fetching the new token or updating our registration
            // data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Log.e(TAG, "Failed to complete token refresh", e);
        }
    }
}

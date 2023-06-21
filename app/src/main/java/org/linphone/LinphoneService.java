package org.linphone;

/*
LinphoneService.java
Copyright (C) 2017  Belledonne Communications, Grenoble, France

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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.view.WindowManager;

import org.linphone.call.CallIncomingActivity;
import org.linphone.contacts.ContactsManager;
import org.linphone.core.Call;
import org.linphone.core.Call.State;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.GlobalState;
import org.linphone.core.LogLevel;
import org.linphone.core.LoggingService;
import org.linphone.core.LoggingServiceListener;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.tools.Log;
import org.linphone.core.tools.service.CoreService;
import org.linphone.mediastream.Version;
import org.linphone.notifications.NotificationsManager;
import org.linphone.receivers.BluetoothManager;
import org.linphone.settings.LinphonePreferences;
import org.linphone.utils.LinphoneUtils;
import org.linphone.views.LinphoneGL2JNIViewOverlay;
import org.linphone.views.LinphoneOverlay;
import org.linphone.views.LinphoneTextureViewOverlay;

import java.util.ArrayList;
import java.util.List;

import it.nethesis.utils.AppBackgroundWatcher;

/**
 * Linphone service, reacting to Incoming calls, ...<br>
 *
 * <p>Roles include:
 *
 * <ul>
 *   <li>Initializing LinphoneManager
 *   <li>Starting C libLinphone through LinphoneManager
 *   <li>Reacting to LinphoneManager state changes
 *   <li>Delegating GUI state change actions to GUI listener
 */
public final class LinphoneService extends CoreService {
    /* Listener needs to be implemented in the Service as it calls
     * setLatestEventInfo and startActivity() which needs a context.
     */
    private static final String START_LINPHONE_LOGS = " ==== Phone information dump ====";

    private static LinphoneService sInstance;

    public final Handler handler = new Handler();
    public final Handler nethCallHandler = new Handler();

    private final boolean mTestDelayElapsed = true;
    private CoreListenerStub mListener;
    private WindowManager mWindowManager;
    private LinphoneOverlay mOverlay;
    private Application.ActivityLifecycleCallbacks mActivityCallbacks;
    private NotificationsManager mNotificationManager;
    private String mIncomingReceivedActivityName;
    private Class<? extends Activity> mIncomingReceivedActivity = CallIncomingActivity.class;
    private Boolean startFromNotif = false;

    private final LoggingServiceListener mJavaLoggingService =
            new LoggingServiceListener() {
                @Override
                public void onLogMessageWritten(
                        LoggingService logService, String domain, LogLevel lev, String message) {
                    switch (lev) {
                        case Debug:
                            android.util.Log.d(domain, message);
                            break;
                        case Message:
                            android.util.Log.i(domain, message);
                            break;
                        case Warning:
                            android.util.Log.w(domain, message);
                            break;
                        case Error:
                            android.util.Log.e(domain, message);
                            break;
                        case Fatal:
                        default:
                            android.util.Log.wtf(domain, message);
                            break;
                    }
                }
            };

    public LoggingServiceListener getJavaLoggingService() {
        return mJavaLoggingService;
    }

    public static boolean isReady() {
        return sInstance != null && sInstance.mTestDelayElapsed;
    }

    public static LinphoneService instance() {
        if (isReady()) return sInstance;

        throw new RuntimeException("LinphoneService not instantiated yet");
    }

    public NotificationsManager getNotificationManager() {
        return mNotificationManager;
    }

    public void removeForegroundServiceNotificationIfPossible() {
        mNotificationManager.removeForegroundServiceNotificationIfPossible();
    }

    public Class<? extends Activity> getIncomingReceivedActivity() {
        return mIncomingReceivedActivity;
    }

    public void setCurrentlyDisplayedChatRoom(String address) {
        if (address != null) {
            mNotificationManager.resetMessageNotifCount(address);
        }
    }

    private void onBackgroundMode() {
        Log.i("[LinphoneService] App has entered background mode");
        if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
            LinphoneManager.getLcIfManagerNotDestroyedOrNull().enterBackground();
        }
    }

    private void onForegroundMode() {
        Log.i("[LinphoneService] App has left background mode");
        if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
            LinphoneManager.getLcIfManagerNotDestroyedOrNull().enterForeground();
        }
    }

    private void setupActivityMonitor() {
        if (mActivityCallbacks != null) return;
        getApplication()
                .registerActivityLifecycleCallbacks(mActivityCallbacks = new ActivityMonitor());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        android.util.Log.i(
                "LinphoneService",
                "[LinphoneService] onStartCommand");

        boolean isPush = false;
        if (intent != null && intent.getBooleanExtra("PushNotification", false)) {
            android.util.Log.i(
                    "LinphoneService",
                    "[LinphoneService] [Push Notification] LinphoneService started because of a push");
            isPush = true;
        }

        boolean startedFromBootReceiver = false;
        if (intent != null && intent.getBooleanExtra("startFromBootReceiver", false)) {
            android.util.Log.i("LinphoneService", "[LinphoneService] LinphoneService started after boot");
            startedFromBootReceiver = true;
        }

        if (sInstance != null) {
            android.util.Log.i(
                    "LinphoneService",
                    "[LinphoneService] Attempt to start the LinphoneService but it is already running !");
            return START_STICKY;
        }

        LinphoneManager.createAndStart(this, isPush);

        startFromNotif = isPush;
        sInstance = this; // sInstance is ready once linphone manager has been created
        mNotificationManager = new NotificationsManager(this);
        LinphoneManager.getLc()
                .addListener(
                        mListener =
                                new CoreListenerStub() {
                                    @Override
                                    public void onCallStateChanged(
                                            Core lc, Call call, Call.State state, String message) {
                                        if (sInstance == null) {
                                            android.util.Log.i(
                                                    "LinphoneService",
                                                    "[LinphoneService] Service not ready, discarding call state change to "
                                                    + state.toString());
                                            return;
                                        }

                                        android.util.Log.i(
                                                "LinphoneService",
                                                "[LinphoneService] State -> " + state.toString());

                                        nethCallHandler.removeCallbacksAndMessages(null);

                                        if (getResources()
                                                .getBoolean(R.bool.enable_call_notification)) {
                                            mNotificationManager.displayCallNotification(call);
                                        }

                                        if (state == Call.State.IncomingReceived
                                                || state == State.IncomingEarlyMedia) {
                                            if (!LinphoneManager.getInstance().getCallGsmON())
                                                onIncomingReceived();
                                        }

                                        if (state == State.End
                                                || state == State.Released
                                                || state == State.Error) {
                                            destroyOverlay();
                                        }

                                        /* destroy service */
                                        if (state == State.Released) {
                                            if (LinphoneManager.getLc().getCalls().length <= 0) {
                                                boolean alwaysOpenServiceFlag = LinphonePreferences.instance().getServiceNotificationVisibility();
                                                boolean status =
                                                        call.getCallLog().getStatus()
                                                                == Call.Status.Missed
                                                                || call.getCallLog().getStatus()
                                                                == Call.Status.Aborted
                                                                || call.getCallLog().getStatus()
                                                                == Call.Status.Declined
                                                                || call.getCallLog().getStatus()
                                                                == Call.Status.AcceptedElsewhere
                                                                || call.getCallLog().getStatus()
                                                                == Call.Status.Success;
                                                if (status
                                                        && call.getDir() == Call.Dir.Incoming
                                                        && startFromNotif && !alwaysOpenServiceFlag) {
                                                    try {
                                                        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Activity.ACTIVITY_SERVICE);
                                                        List<ActivityManager.AppTask> list = am.getAppTasks();
                                                        for (ActivityManager.AppTask task : list) {
                                                            task.finishAndRemoveTask();
                                                        }
                                                    } catch (Exception ex) {
                                                        android.util.Log.w("LinphoneService", ex.getLocalizedMessage());
                                                    }

                                                    stopSelf();
                                                }
                                            }
                                            if (call.getCallLog().getStatus()
                                                    == Call.Status.Missed) {
                                                mNotificationManager.displayMissedCallNotification(
                                                        call);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onGlobalStateChanged(
                                            Core lc, GlobalState state, String message) {
                                        // TODO global state if ON
                                    }

                                    @Override
                                    public void onRegistrationStateChanged(
                                            Core lc,
                                            ProxyConfig cfg,
                                            RegistrationState state,
                                            String smessage) {
                                        // TODO registration status
                                    }
                                });

        if (Version.sdkAboveOrEqual(Version.API26_O_80)) {
            mNotificationManager.startForeground();
        }

        if (!Version.sdkAboveOrEqual(Version.API26_O_80)
                || (ContactsManager.getInstance() != null
                && ContactsManager.getInstance().hasReadContactsAccess())) {
            getContentResolver()
                    .registerContentObserver(
                            ContactsContract.Contacts.CONTENT_URI,
                            true,
                            ContactsManager.getInstance());
        }

        BluetoothManager.getInstance().initBluetooth();

        /* Close Service If Started From Service */
        android.util.Log.i(
                "LinphoneService", "Calls: " + LinphoneManager.getLc().getCalls().length);
        if (startedFromBootReceiver) {
            try {
                boolean alwaysOpenServiceFlag =
                        LinphonePreferences.instance().getServiceNotificationVisibility();
                boolean noCallOngoing = LinphoneManager.getLc().getCalls().length <= 0;
                android.util.Log.i(
                        "LinphoneService",
                        "alwaysOpenServiceFlag: "
                                + alwaysOpenServiceFlag
                                + ". noCallOngoing: "
                                + noCallOngoing);
                if (!alwaysOpenServiceFlag && noCallOngoing) {
                    android.util.Log.i(
                            "LinphoneService",
                            "AppBackgroundWatcher: " + AppBackgroundWatcher.INSTANCE.getValue());
                    if (AppBackgroundWatcher.INSTANCE.getValue() == null
                            || !AppBackgroundWatcher.INSTANCE.getValue()) {
                        stopSelf();
                    }
                }
            } catch (Exception e) {
                Log.e(e);
                android.util.Log.e("LinphoneService", e.getLocalizedMessage());
            }
        }

        final boolean isPushFinal = isPush;
        nethCallHandler.removeCallbacksAndMessages(null);
        nethCallHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        android.util.Log.d(
                                "LinphoneService",
                                "Timer triggerato. Provo a chiudere il servizio.");
                        try {
                            boolean alwaysOpenServiceFlag =
                                    LinphonePreferences.instance()
                                            .getServiceNotificationVisibility();
                            boolean noCallOngoing = LinphoneManager.getLc().getCalls().length <= 0;
                            android.util.Log.d(
                                    "LinphoneService",
                                    "alwaysOpenServiceFlag: "
                                            + alwaysOpenServiceFlag
                                            + ". noCallOngoing: "
                                            + noCallOngoing);
                            if (!alwaysOpenServiceFlag && noCallOngoing && isPushFinal) {
                                stopSelf();
                            }
                        } catch (Exception e) {
                            android.util.Log.w("LinphoneService", e.getLocalizedMessage());
                            stopSelf(); // Are you sure?
                        }
                    }
                },
                60000);

        return START_STICKY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
        super.onCreate();

        setupActivityMonitor();

        // Needed in order for the two next calls to succeed, libraries must have been loaded first
        LinphonePreferences.instance().setContext(getBaseContext());
        Factory.instance().setLogCollectionPath(getFilesDir().getAbsolutePath());
        boolean isDebugEnabled = LinphonePreferences.instance().isDebugEnabled();
        LinphoneUtils.configureLoggingService(isDebugEnabled, getString(R.string.app_name));
        // LinphoneService isn't ready yet so we have to manually set up the Java logging service
        if (LinphonePreferences.instance().useJavaLogger()) {
            Factory.instance().getLoggingService().addListener(mJavaLoggingService);
        }

        // Dump some debugging information to the logs
        Log.i(START_LINPHONE_LOGS);
        dumpDeviceInformation();
        dumpInstalledLinphoneInformation();

        mIncomingReceivedActivityName =
                LinphonePreferences.instance().getActivityToLaunchOnIncomingReceived();
        try {
            mIncomingReceivedActivity =
                    (Class<? extends Activity>) Class.forName(mIncomingReceivedActivityName);
        } catch (ClassNotFoundException e) {
            Log.e(e);
        }

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    public void createOverlay() {
        if (mOverlay != null) destroyOverlay();

        Core core = LinphoneManager.getLc();
        Call call = core.getCurrentCall();
        if (call == null || !call.getCurrentParams().videoEnabled()) return;

        if ("MSAndroidOpenGLDisplay".equals(core.getVideoDisplayFilter())) {
            mOverlay = new LinphoneGL2JNIViewOverlay(this);
        } else {
            mOverlay = new LinphoneTextureViewOverlay(this);
        }
        WindowManager.LayoutParams params = mOverlay.getWindowManagerLayoutParams();
        params.x = 0;
        params.y = 0;
        mOverlay.addToWindowManager(mWindowManager, params);
    }

    public void destroyOverlay() {
        if (mOverlay != null) {
            mOverlay.removeFromWindowManager(mWindowManager);
            mOverlay.destroy();
        }
        mOverlay = null;
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Supported ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        sb.append("\n");
        Log.i(sb.toString());
    }

    private void dumpInstalledLinphoneInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException nnfe) {
            Log.e(nnfe);
        }

        if (info != null) {
            Log.i(
                    "[LinphoneService] Linphone version is ",
                    info.versionName + " (" + info.versionCode + ")");
        } else {
            Log.i("[LinphoneService] Linphone version is unknown");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        boolean serviceNotif = LinphonePreferences.instance().getServiceNotificationVisibility();
        if (serviceNotif) {
            Log.i("[LinphoneService] Service is running in foreground, don't stop it");
        } else if (getResources().getBoolean(R.bool.kill_service_with_task_manager)) {
            Log.i("[LinphoneService] Task removed, stop service");
            Core lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            if (lc != null) {
                lc.terminateAllCalls();
            }

            // If push is enabled, don't unregister account, otherwise do unregister
            if (LinphonePreferences.instance().isPushNotificationEnabled()) {
                if (lc != null) lc.setNetworkReachable(false);
            }
            stopSelf();
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public synchronized void onDestroy() {
        if (mActivityCallbacks != null) {
            getApplication().unregisterActivityLifecycleCallbacks(mActivityCallbacks);
            mActivityCallbacks = null;
        }
        destroyOverlay();

        Core lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }

        // Make sure our notification is gone.
        if (mNotificationManager != null) {
            removeForegroundServiceNotificationIfPossible();
        }

        sInstance = null;
        lc = null; // To allow the gc calls below to free the Core
        LinphoneManager.destroy();

        // This will prevent the app from crashing if the service gets killed in background mode
        if (LinphoneActivity.isInstanciated()) {
            Log.w("[LinphoneService] Service is getting destroyed, finish LinphoneActivity");
            LinphoneActivity.instance().finish();
        }

        if (LinphonePreferences.instance().useJavaLogger()) {
            Factory.instance().getLoggingService().removeListener(mJavaLoggingService);
        }

        super.onDestroy();
    }

    @SuppressWarnings("unchecked")
    public void setActivityToLaunchOnIncomingReceived(String activityName) {
        try {
            mIncomingReceivedActivity = (Class<? extends Activity>) Class.forName(activityName);
            mIncomingReceivedActivityName = activityName;
            LinphonePreferences.instance()
                    .setActivityToLaunchOnIncomingReceived(mIncomingReceivedActivityName);
        } catch (ClassNotFoundException e) {
            Log.e(e);
        }
    }

    private void onIncomingReceived() {
        android.util.Log.i(
                "LinphoneService",
                "[LinphoneService] Start LinphoneActivity");
        Intent intent = new Intent(this, mIncomingReceivedActivity);
        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().startActivity(intent);
        } else {
            // This flag is required to start an Activity from a Service context
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /*Believe me or not, but knowing the application visibility state on Android is a nightmare.
    After two days of hard work I ended with the following class, that does the job more or less reliabily.
    */
    class ActivityMonitor implements Application.ActivityLifecycleCallbacks {
        private final ArrayList<Activity> activities = new ArrayList<>();
        private boolean mActive = false;
        private int mRunningActivities = 0;
        private InactivityChecker mLastChecker;

        @Override
        public synchronized void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i("[LinphoneService] Activity created:" + activity);
            if (!activities.contains(activity)) activities.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.i("Activity started:" + activity);
        }

        @Override
        public synchronized void onActivityResumed(Activity activity) {
            Log.i("[LinphoneService] Activity resumed:" + activity);
            if (activities.contains(activity)) {
                mRunningActivities++;
                Log.i("[LinphoneService] runningActivities=" + mRunningActivities);
                checkActivity();
            }
        }

        @Override
        public synchronized void onActivityPaused(Activity activity) {
            Log.i("[LinphoneService] Activity paused:" + activity);
            if (activities.contains(activity)) {
                mRunningActivities--;
                Log.i("[LinphoneService] runningActivities=" + mRunningActivities);
                checkActivity();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.i("[LinphoneService] Activity stopped:" + activity);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public synchronized void onActivityDestroyed(Activity activity) {
            Log.i("[LinphoneService] Activity destroyed:" + activity);
            activities.remove(activity);
        }

        void startInactivityChecker() {
            if (mLastChecker != null) mLastChecker.cancel();
            LinphoneService.this.handler.postDelayed(
                    (mLastChecker = new InactivityChecker()), 2000);
        }

        void checkActivity() {
            if (mRunningActivities == 0) {
                if (mActive) startInactivityChecker();
            } else if (mRunningActivities > 0) {
                if (!mActive) {
                    mActive = true;
                    LinphoneService.this.onForegroundMode();
                }
                if (mLastChecker != null) {
                    mLastChecker.cancel();
                    mLastChecker = null;
                }
            }
        }

        class InactivityChecker implements Runnable {
            private boolean isCanceled;

            void cancel() {
                isCanceled = true;
            }

            @Override
            public void run() {
                synchronized (LinphoneService.this) {
                    if (!isCanceled) {
                        if (ActivityMonitor.this.mRunningActivities == 0 && mActive) {
                            mActive = false;
                            LinphoneService.this.onBackgroundMode();
                        }
                    }
                }
            }
        }
    }

    public void deleteTimer() {
        nethCallHandler.removeCallbacksAndMessages(null);
    }

}

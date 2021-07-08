package org.linphone

import android.app.Application
import android.os.Handler
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.nethesis.utils.AppBackgroundWatcher
import org.linphone.settings.LinphonePreferences

class NethCTIApplication : Application(), LifecycleObserver {

    companion object {

        private lateinit var _instance: NethCTIApplication

        val instance: NethCTIApplication
            get() {
                return _instance
            }
    }

    val backgroundWatcher: AppBackgroundWatcher = AppBackgroundWatcher
    val killAppHandler = Handler()

    override fun onCreate() {
        super.onCreate()
        _instance = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
        backgroundWatcher.value = false
        killAppHandler.removeCallbacksAndMessages(null)
        if (LinphoneService.isReady()) {
            Log.d("LinphoneService", "rimuovo timer perchè in foreground")
            LinphoneService.instance()?.deleteTimer()
        }
        Log.d("WEDO", "App in foreground.")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        // app moved to background
        Log.d("WEDO", "App in background.")
        backgroundWatcher.value = true
        killAppHandler.postDelayed({
            LinphoneManager.getLcIfManagerNotDestroyedOrNull()?.let {
                if (it.calls.isEmpty() && !LinphonePreferences.instance().serviceNotificationVisibility && LinphoneService.isReady()
                ) {
                    Log.d("WEDO", "Service killed.")
                    LinphoneService.instance().stopSelf()
                }
            }
        }, 60000)
    }

}
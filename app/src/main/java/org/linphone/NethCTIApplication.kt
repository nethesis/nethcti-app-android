package org.linphone

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
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

    override fun onCreate() {
        super.onCreate()
        _instance = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
        backgroundWatcher.value = false
        if(LinphoneService.isReady()) {
            android.util.Log.d("LinphoneService", "rimuovo timer perchè in foreground");
            LinphoneService.instance()?.deleteTimer()
        }
        Log.d("WEDO", "App in foreground.")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        // app moved to background
        backgroundWatcher.value = true
        Log.d("WEDO", "App in background.")
        //killService
        LinphoneManager.getLcIfManagerNotDestroyedOrNull()?.let {
            if (it.calls.isEmpty() && !LinphonePreferences.instance().serviceNotificationVisibility && LinphoneService.isReady()
            ) {
                Log.d("WEDO", "Service killed.")
                LinphoneService.instance().stopSelf()
            }
        }
    }
}
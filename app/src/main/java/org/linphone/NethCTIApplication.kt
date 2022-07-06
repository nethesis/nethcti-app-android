package org.linphone

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.nethesis.models.NethUser
import it.nethesis.utils.AppBackgroundWatcher
import it.nethesis.webservices.RetrofitGenerator
import it.nethesis.webservices.UserRestAPI
import org.linphone.settings.LinphonePreferences
import org.linphone.utils.SharedPreferencesManager
import org.linphone.utils.expireIfProxyConfigured
import org.linphone.utils.expireIfProxyNotConfigured
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NethCTIApplication : Application(), LifecycleObserver {

    companion object {

        private lateinit var _instance: NethCTIApplication

        val instance: NethCTIApplication
            get() {
                return _instance
            }

        val dayNightThemeColor: Boolean
            get() {
                return LinphonePreferences.instance().config.getBool(
                    "app",
                    "dark_mode", AppCompatDelegate.getDefaultNightMode()
                        == MODE_NIGHT_YES
                )
            }

    }

    private val backgroundWatcher: AppBackgroundWatcher = AppBackgroundWatcher
    private val killAppHandler = Handler()

    override fun onCreate() {
        super.onCreate()
        _instance = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        val darkMode = LinphonePreferences.instance().apply {
            setContext(instance)
        }.config.getBool(
            "app",
            "dark_mode", AppCompatDelegate.getDefaultNightMode()
                    == MODE_NIGHT_YES
        )
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) {
                MODE_NIGHT_YES
            } else {
                MODE_NIGHT_NO
            }
        )

        /* Check if logged and retrieve main ext if not present */
        getMainExtensionIfNecessary()
    }

    private fun getMainExtensionIfNecessary() {
        val authToken = SharedPreferencesManager.getAuthtoken(this)
        val domain = SharedPreferencesManager.getDomain(this)
        val mainExtension = SharedPreferencesManager.getMainExtension(this)
        if (isNetworkOnline(this) && !authToken.isNullOrEmpty() && !domain.isNullOrEmpty() && mainExtension.isNullOrEmpty()) {
            val userRestAPI = RetrofitGenerator.createService(
                UserRestAPI::class.java, domain
            )
            userRestAPI.getMe(authToken).enqueue(
                object : Callback<NethUser?> {
                    override fun onResponse(call: Call<NethUser?>, response: Response<NethUser?>) {
                        val nethUser = response.body()
                        runCatching {
                            if (nethUser?.endpoints?.mainextension?.get(0) != null) {
                                val mainExt = nethUser.endpoints.mainextension[0].id
                                SharedPreferencesManager.setMainExtension(
                                    applicationContext, mainExt
                                )
                            }
                            /* Refresh Expire */
                            val proxyPort = nethUser?.endpoints?.extension?.find { it.type == "mobile" }?.proxyPort
                            val core = LinphoneManager.getLcIfManagerNotDestroyedOrNull()
                            val proxyConfig = core.defaultProxyConfig
                            if(proxyPort != null) {
                                proxyConfig.expires = expireIfProxyConfigured
                            } else {
                                proxyConfig.expires = expireIfProxyNotConfigured
                            }
                            core.clearProxyConfig() //I can have only one account registered at time
                            core.addProxyConfig(proxyConfig)
                            core.defaultProxyConfig = proxyConfig

                        }
                    }

                    override fun onFailure(call: Call<NethUser?>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
        }
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

    private fun isNetworkOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

}
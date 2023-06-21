package org.linphone.receivers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import org.linphone.LinphoneManager
import org.linphone.utils.AudioRouteMethod

/**
 * Pause current SIP calls when GSM phone rings or is active.
 */
class PhoneStateChangedReceiver : BroadcastReceiver() {
    //Not the safest method to store old values. If multiple class are received this will be unsynced
    private var routeMethod = AudioRouteMethod.Earpiece

    override fun onReceive(context: Context, intent: Intent) {
        val extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (!LinphoneManager.isInstanciated()) return
        Log.w("WEDOASD", "Stato: $extraState")
        when(extraState) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                resetAudioRouting()
                LinphoneManager.getInstance().callGsmON = true
                val lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull()
                lc?.pauseAllCalls()
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                resetAudioRouting()
                LinphoneManager.getInstance().callGsmON = false
            }
            TelephonyManager.EXTRA_STATE_RINGING -> {
                routeMethod = getActiveRouteMethod()
                if (BluetoothManager.getInstance().isBluetoothHeadsetAvailable) {
                    BluetoothManager.getInstance().routeAudioToBluetooth()
                } else {
                    LinphoneManager.getInstance().routeAudioToReceiver()
                }

                LinphoneManager.getInstance().callGsmON = false
            }
        }
    }

    private fun getActiveRouteMethod(): AudioRouteMethod {
        if(!LinphoneManager.isInstanciated()) {
            return AudioRouteMethod.Earpiece //def
        }
        return when {
            LinphoneManager.getInstance().isSpeakerEnabled -> AudioRouteMethod.Speaker
            LinphoneManager.getInstance().isBluetoothHeadSetEnabled -> AudioRouteMethod.Bluetooth
            else -> AudioRouteMethod.Earpiece
        }
    }

    private fun resetAudioRouting() {
        when(routeMethod) {
            AudioRouteMethod.Earpiece -> LinphoneManager.getInstance().routeAudioToReceiver()
            AudioRouteMethod.Speaker -> LinphoneManager.getInstance().routeAudioToSpeaker()
            AudioRouteMethod.Bluetooth -> BluetoothManager.getInstance().routeAudioToBluetooth()
        }
    }
}
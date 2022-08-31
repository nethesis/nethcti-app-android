package org.linphone.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import org.linphone.LinphoneManager
import org.linphone.utils.AudioRouteMethod

/**
 * Pause current SIP calls when GSM phone rings or is active.
 */
class PhoneStateChangedReceiver : BroadcastReceiver() {
    //Not the safest method to store old values. If multiple class are received this will be unsynced
    private val routeMethod: HashMap<String, AudioRouteMethod> = hashMapOf()

    override fun onReceive(context: Context, intent: Intent) {
        val extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val extraNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "" //Deprecated after API29
        if (!LinphoneManager.isInstanciated()) return
        when (extraState) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                resetAudioRouting(extraNumber)
                LinphoneManager.getInstance().callGsmON = true
                val lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull()
                lc?.pauseAllCalls()
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                resetAudioRouting(extraNumber)
                LinphoneManager.getInstance().callGsmON = false
            }
            TelephonyManager.EXTRA_STATE_RINGING -> {
                getActiveRouteMethod(extraNumber)
                if (BluetoothManager.getInstance().isBluetoothHeadsetAvailable) {
                    BluetoothManager.getInstance().routeAudioToBluetooth()
                } else {
                    LinphoneManager.getInstance().routeAudioToReceiver()
                }

                LinphoneManager.getInstance().callGsmON = false
            }
        }
    }

    private fun getActiveRouteMethod(number: String) {
        if (!LinphoneManager.isInstanciated()) {
            return
        }
        val audioRoute = when {
            LinphoneManager.getInstance().isSpeakerEnabled -> AudioRouteMethod.Speaker
            LinphoneManager.getInstance().isBluetoothHeadSetEnabled -> AudioRouteMethod.Bluetooth
            else -> AudioRouteMethod.Earpiece
        }

        routeMethod[number] = audioRoute
    }

    private fun resetAudioRouting(number: String) {
        routeMethod[number]?.let {
            when (it) {
                AudioRouteMethod.Earpiece -> LinphoneManager.getInstance().routeAudioToReceiver()
                AudioRouteMethod.Speaker -> LinphoneManager.getInstance().routeAudioToSpeaker()
                AudioRouteMethod.Bluetooth -> BluetoothManager.getInstance().routeAudioToBluetooth()
            }
        }
    }
}
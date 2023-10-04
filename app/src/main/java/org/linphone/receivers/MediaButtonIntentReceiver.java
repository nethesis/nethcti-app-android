package org.linphone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import org.linphone.LinphoneManager;

/**
 * Created by Mirko Pruiti on 03/10/23.
 *
 * @author Mirko Pruiti
 */
public class MediaButtonIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.i("MediaButtonIntentReceiver","onReceive");
        String intentAction = intent.getAction();
        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            android.util.Log.i("MediaButtonIntentReceiver","[event] " + event);
            if (event == null) {
                return;
            }
            int keycode = event.getKeyCode();
            android.util.Log.i("MediaButtonIntentReceiver","[Keycode] " + keycode);
            if (LinphoneManager.isInstanciated()) {
                if(LinphoneManager.getLc().getCurrentCall() != null){
                    switch (keycode) {
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                            LinphoneManager.getLc().getCurrentCall().terminate();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            LinphoneManager.getLc().getCurrentCall().pause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            LinphoneManager.getLc().getCurrentCall().resume();
                            break;
                        case KeyEvent.KEYCODE_VOLUME_UP:
                            LinphoneManager.getInstance().adjustVolume(1);
                            break;
                        case KeyEvent.KEYCODE_VOLUME_DOWN:
                            LinphoneManager.getInstance().adjustVolume(-1);
                            break;
                        case KeyEvent.KEYCODE_VOLUME_MUTE:
                            LinphoneManager.getLc().getCurrentCall().setSpeakerMuted(!LinphoneManager.getLc().getCurrentCall().getSpeakerMuted());
                            break;
                    }
                }
            }
        }
    }
}
package org.linphone;

/*
LinphoneLauncherActivity.java
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

import static android.content.Intent.ACTION_MAIN;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.linphone.assistant.AssistantActivity;
import org.linphone.assistant.RemoteProvisioningActivity;
import org.linphone.settings.LinphonePreferences;

/**
 * Launch Linphone main activity when Service is ready.
 */
public class LinphoneLauncherActivity extends Activity {

    private Handler mHandler;
    private ServiceWaitThread mServiceThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hack to avoid to draw twice LinphoneActivity on tablets
        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (getResources().getBoolean(R.bool.use_full_screen_image_splashscreen)) {
            setContentView(R.layout.launch_screen_full_image);
        } else {
            setContentView(R.layout.launch_screen);
        }

        mHandler = new Handler();

        if (LinphoneService.isReady()) {
            onServiceReady();
        } else {
            Intent i = new Intent(ACTION_MAIN).setClass(this, LinphoneService.class);
            // start linphone as background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                i.putExtra(LinphoneService.FOREGROUND_KEY, true);
                startForegroundService(i);
            } else {
                startService(i);
            }
            mServiceThread = new ServiceWaitThread();
            mServiceThread.start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View root = super.onCreateView(name, context, attrs);
        /* Make splash screen full screen and color status bar*/
        boolean isStatusBarLight = getResources().getBoolean(R.bool.splashScreenLightStatusBar);
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if(isStatusBarLight) {
            flags = flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(flags);
        return root;
    }

    private void onServiceReady() {
        final Class<? extends Activity> classToStart;
        /*if (getResources().getBoolean(R.bool.show_tutorials_instead_of_app)) {
        	classToStart = TutorialLauncherActivity.class;
        } else */
        if (getResources().getBoolean(R.bool.display_sms_remote_provisioning_activity)
                && LinphonePreferences.instance().isFirstRemoteProvisioning()) {
            classToStart = RemoteProvisioningActivity.class;
        } else {
            if (LinphoneManager.getLc().getProxyConfigList() != null
                    && LinphoneManager.getLc().getProxyConfigList().length == 0) {
                classToStart = AssistantActivity.class;
            } else {
                classToStart = LinphoneActivity.class;
            }
        }

        mHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        android.util.Log.d("ASD", "LightStatusBAar: " + getResources().getBoolean(R.bool.splashScreenLightStatusBar));
                        startActivity(
                                getIntent().setClass(LinphoneLauncherActivity.this, classToStart));
                    }
                },
                5000);

        LinphoneManager.getInstance().changeStatusToOnline();
    }

    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            mHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            onServiceReady();
                        }
                    });
            mServiceThread = null;
        }
    }
}

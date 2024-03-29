package org.linphone.settings;

/*
AdvancedSettingsFragment.java
Copyright (C) 2019 Belledonne Communications, Grenoble, France

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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import org.linphone.LinphoneActivity;
import org.linphone.LinphoneService;
import org.linphone.R;
import org.linphone.fragments.FragmentsAvailable;
import org.linphone.settings.widget.BasicSetting;
import org.linphone.settings.widget.SettingListenerBase;
import org.linphone.settings.widget.SwitchSetting;
import org.linphone.settings.widget.TextSetting;

public class AdvancedSettingsFragment extends Fragment {
    protected View mRootView;
    protected LinphonePreferences mPrefs;

    private SwitchSetting mDebug,
            mJavaLogger,
            mFriendListSubscribe,
            mBackgroundMode,
            mStartAtBoot,
            mDarkMode;
    private TextSetting mRemoteProvisioningUrl, mDisplayName, mUsername, mDeviceName;
    private BasicSetting mAndroidAppSettings;
    private LinearLayout mPrefPrimAccount;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.settings_advanced, container, false);

        loadSettings();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPrefs = LinphonePreferences.instance();
        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance()
                    .selectMenu(
                            FragmentsAvailable.SETTINGS_SUBLEVEL,
                            getString(R.string.pref_advanced_title));
        }

        updateValues();
    }

    protected void loadSettings() {
        mDebug = mRootView.findViewById(R.id.pref_debug);

        mJavaLogger = mRootView.findViewById(R.id.pref_java_debug);
        // This is only required for blackberry users for all we know
        mJavaLogger.setVisibility(
                Build.MANUFACTURER.equals("BlackBerry") ? View.VISIBLE : View.GONE);

        mFriendListSubscribe = mRootView.findViewById(R.id.pref_friendlist_subscribe);
        if (!getResources().getBoolean(R.bool.neth_show_pref_friend_list)) {
            mFriendListSubscribe.setVisibility(View.GONE);
        }

        mBackgroundMode = mRootView.findViewById(R.id.pref_background_mode);

        mStartAtBoot = mRootView.findViewById(R.id.pref_autostart);
        if (!getResources().getBoolean(R.bool.neth_show_pref_autostart)) {
            mStartAtBoot.setVisibility(View.GONE);
        }

        mDarkMode = mRootView.findViewById(R.id.pref_dark_mode);
        mDarkMode.setVisibility(View.GONE);

        mRemoteProvisioningUrl = mRootView.findViewById(R.id.pref_remote_provisioning);
        mRemoteProvisioningUrl.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        mDisplayName = mRootView.findViewById(R.id.pref_display_name);

        mUsername = mRootView.findViewById(R.id.pref_user_name);

        mAndroidAppSettings = mRootView.findViewById(R.id.pref_android_app_settings);

        mDeviceName = mRootView.findViewById(R.id.pref_device_name);

        mPrefPrimAccount = mRootView.findViewById(R.id.pref_android_prim_account);
        if (!getResources().getBoolean(R.bool.neth_show_pref_android_prim_account)) {
            mPrefPrimAccount.setVisibility(View.GONE);
        }
    }

    protected void setListeners() {
        mDebug.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        mPrefs.setDebugEnabled(newValue);
                    }
                });

        mJavaLogger.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        mPrefs.setJavaLogger(newValue);
                    }
                });

        if (getResources().getBoolean(R.bool.neth_show_pref_friend_list)) {
            mFriendListSubscribe.setListener(
                    new SettingListenerBase() {
                        @Override
                        public void onBoolValueChanged(boolean newValue) {
                            mPrefs.enabledFriendlistSubscription(newValue);
                        }
                    });
        }

        mBackgroundMode.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        mPrefs.setServiceNotificationVisibility(newValue);
                        if (newValue) {
                            LinphoneService.instance().getNotificationManager().startForeground();
                        } else {
                            LinphoneService.instance().getNotificationManager().stopForeground();
                        }
                    }
                });

        if (getResources().getBoolean(R.bool.neth_show_pref_autostart)) {
            mStartAtBoot.setListener(
                    new SettingListenerBase() {
                        @Override
                        public void onBoolValueChanged(boolean newValue) {
                            mPrefs.setAutoStart(newValue);
                        }
                    });
        }

        mDarkMode.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        mPrefs.enableDarkMode(newValue);
                        /* Switch status bar icon color */
                        Activity activity = getActivity();
                        if(activity != null) {
                            Window window = activity.getWindow();
                            if(window != null) {
                                new WindowInsetsControllerCompat(window, window.getDecorView()).setAppearanceLightStatusBars(!newValue);
                            }
                        }
                    }
                });

        mRemoteProvisioningUrl.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        mPrefs.setRemoteProvisioningUrl(newValue);
                    }
                });

        mDisplayName.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        mPrefs.setDefaultDisplayName(newValue);
                    }
                });

        mUsername.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        mPrefs.setDefaultUsername(newValue);
                    }
                });

        mAndroidAppSettings.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        Context context = LinphoneActivity.instance();
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + context.getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(i, LinphoneActivity.ANDROID_APP_SETTINGS_ACTIVITY);
                    }
                });

        mDeviceName.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        mPrefs.setDeviceName(newValue);
                    }
                });
    }

    protected void updateValues() {
        mDebug.setChecked(mPrefs.isDebugEnabled());

        mJavaLogger.setChecked(mPrefs.useJavaLogger());

        if (getResources().getBoolean(R.bool.neth_show_pref_friend_list)) {
            mFriendListSubscribe.setChecked(mPrefs.isFriendlistsubscriptionEnabled());
        }

        mBackgroundMode.setChecked(mPrefs.getServiceNotificationVisibility());

        if (getResources().getBoolean(R.bool.neth_show_pref_autostart)) {
            mStartAtBoot.setChecked(mPrefs.isAutoStartEnabled());
        }

        mDarkMode.setChecked(mPrefs.isDarkModeEnabled());

        mRemoteProvisioningUrl.setValue(mPrefs.getRemoteProvisioningUrl());

        mDisplayName.setValue(mPrefs.getDefaultDisplayName());

        mUsername.setValue(mPrefs.getDefaultUsername());

        mDeviceName.setValue(mPrefs.getDeviceName(LinphoneActivity.instance()));

        setListeners();
    }
}

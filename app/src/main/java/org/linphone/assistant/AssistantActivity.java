package org.linphone.assistant;
/*
AssistantActivity.java
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

import static org.linphone.utils.ConstantsKt.expireIfProxyConfigured;
import static org.linphone.utils.ConstantsKt.expireIfProxyNotConfigured;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.jetbrains.annotations.NotNull;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneLauncherActivity;
import org.linphone.LinphoneManager;
import org.linphone.LinphoneService;
import org.linphone.R;
import org.linphone.core.AccountCreator;
import org.linphone.core.AccountCreatorListener;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.DialPlan;
import org.linphone.core.Factory;
import org.linphone.core.MediaEncryption;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;
import org.linphone.fragments.StatusFragment;
import org.linphone.notifications.FCMNotification;
import org.linphone.settings.LinphonePreferences;
import org.linphone.utils.LinphoneUtils;
import org.linphone.utils.PushNotificationUtils;
import org.linphone.utils.ThemableActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssistantActivity extends ThemableActivity
        implements OnClickListener,
                ActivityCompat.OnRequestPermissionsResultCallback,
                AccountCreatorListener {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 201;
    private static final int PERMISSIONS_REQUEST_CAMERA = 202;

    private static AssistantActivity sInstance;

    public DialPlan country;

    private ImageView mBack /*, mCancel*/;
    private AssistantFragmentsEnum mCurrentFragment;
    private AssistantFragmentsEnum mLastFragment;
    private AssistantFragmentsEnum mFirstFragment;
    private Fragment mFragment;
    private LinphonePreferences mPrefs;
    private boolean mAccountCreated = false,
            mNewAccount = false,
            mIsLink = false,
            mFromPref = false;
    private CoreListenerStub mListener;
    private Address mAddress;
    private StatusFragment mStatus;
    private ProgressDialog mProgress;
    private Dialog mDialog;
    private boolean mRemoteProvisioningInProgress;
    private boolean mEchoCancellerAlreadyDone;
    private boolean mLoginInProgress;
    private AccountCreator mAccountCreator;
    private CountryListAdapter mCountryListAdapter;
    private LinearLayout mTopBar;
    /* SideMenu */
    private DrawerLayout mSideMenu;
    private RelativeLayout mSideMenuContent, mQuitLayout;
    private ListView  mSideMenuItemList;
    private ImageView mMenu;
    private LinphoneActivity.MenuAdapter menuAdapter;
    private static List<LinphoneActivity.MenuItem> mSideMenuItems;
    // private boolean mCallTransfer = false;

    /** Used to show or hide the login and logout menu item. */
    private boolean mustHideLogin;

    public static AssistantActivity instance() {
        return sInstance;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.assistant);
        initUI();

        if (getIntent().getBooleanExtra("LinkPhoneNumber", false)) {
            mIsLink = true;
            if (getIntent().getBooleanExtra("FromPref", false)) mFromPref = true;
            displayCreateAccount();
        } else {
            /*
             * This fragment is used to show a fragment at startup.
             * I load every time the Sip Login Fragment.
             */
            /*
            mFirstFragment =
                    getResources().getBoolean(R.bool.assistant_use_linphone_login_as_first_fragment)
                            ? AssistantFragmentsEnum.LINPHONE_LOGIN
                            : AssistantFragmentsEnum.WELCOME;
            if (mFirstFragment == AssistantFragmentsEnum.WELCOME) {
                mFirstFragment =
                        getResources()
                                        .getBoolean(
                                                R.bool.assistant_use_create_linphone_account_as_first_fragment)
                                ? AssistantFragmentsEnum.CREATE_ACCOUNT
                                : AssistantFragmentsEnum.WELCOME;
            }
            */

            mFirstFragment = AssistantFragmentsEnum.LOGIN; // This is the Sip Login Fragment.
            if (findViewById(R.id.fragment_container) != null) {
                if (savedInstanceState == null) {
                    display(mFirstFragment);
                } else {
                    mCurrentFragment =
                            (AssistantFragmentsEnum)
                                    savedInstanceState.getSerializable("CurrentFragment");
                }
            }
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("echoCanceller")) {
            mEchoCancellerAlreadyDone = savedInstanceState.getBoolean("echoCanceller");
        } else {
            mEchoCancellerAlreadyDone = false;
        }
        mPrefs = LinphonePreferences.instance();

        initSideMenu();

        if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
            mAccountCreator =
                    LinphoneManager.getLc()
                            .createAccountCreator(LinphonePreferences.instance().getXmlrpcUrl());
            mAccountCreator.setListener(this);
        }

        mCountryListAdapter = new CountryListAdapter(getApplicationContext());
        mListener =
                new CoreListenerStub() {

                    @Override
                    public void onConfiguringStatus(
                            Core lc, final ConfiguringState state, String message) {
                        if (mProgress != null) mProgress.dismiss();
                        if (state == ConfiguringState.Successful) {
                            goToLinphoneActivity();
                        } else if (state == ConfiguringState.Failed) {
                            Toast.makeText(
                                            AssistantActivity.instance(),
                                            getString(R.string.remote_provisioning_failure),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onRegistrationStateChanged(
                            Core lc, ProxyConfig cfg, RegistrationState state, String smessage) {
                        if (mRemoteProvisioningInProgress) {
                            if (mProgress != null) mProgress.dismiss();
                            if (state == RegistrationState.Ok) {
                                mRemoteProvisioningInProgress = false;
                                success();
                            }
                        } else if (mAccountCreated && !mNewAccount) {
                            if (mAddress != null
                                    && mAddress.asString()
                                            .equals(cfg.getIdentityAddress().asString())) {
                                if (state == RegistrationState.Ok) {
                                    if (mProgress != null) mProgress.dismiss();
                                    if (getResources()
                                                    .getBoolean(R.bool.use_phone_number_validation)
                                            && cfg.getDomain()
                                                    .equals(getString(R.string.default_domain))
                                            && LinphoneManager.getLc().getDefaultProxyConfig()
                                                    != null) {
                                        loadAccountCreator(cfg).isAccountExist();
                                    } else {
                                        success();
                                    }
                                } else if (state == RegistrationState.Failed) {
                                    if (mProgress != null) mProgress.dismiss();
                                    if (mDialog == null || !mDialog.isShowing()) {
                                        mDialog = createErrorDialog(cfg, smessage);
                                        mDialog.setCancelable(false);
                                        mDialog.show();
                                    }
                                } else if (!(state == RegistrationState.Progress)) {
                                    if (mProgress != null) mProgress.dismiss();
                                }
                            }
                        }
                    }
                };
        sInstance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Core lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }
    }

    @Override
    protected void onPause() {
        Core lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("CurrentFragment", mCurrentFragment);
        outState.putBoolean("echoCanceller", mEchoCancellerAlreadyDone);
        super.onSaveInstanceState(outState);
    }

    public void updateStatusFragment(StatusFragment fragment) {
        mStatus = fragment;
    }

    private AccountCreator loadAccountCreator(ProxyConfig cfg) {
        ProxyConfig cfgTab[] = LinphoneManager.getLc().getProxyConfigList();
        int n = -1;
        for (int i = 0; i < cfgTab.length; i++) {
            if (cfgTab[i].equals(cfg)) {
                n = i;
                break;
            }
        }
        if (n >= 0) {
            mAccountCreator.setDomain(mPrefs.getAccountDomain(n));
            mAccountCreator.setUsername(mPrefs.getAccountUsername(n));
        }
        return mAccountCreator;
    }

    private void initUI() {
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(this);
        // mCancel = findViewById(R.id.assistant_cancel);
        // mCancel.setOnClickListener(this);

        mTopBar = findViewById(R.id.topbar);
        if (getResources().getBoolean(R.bool.assistant_hide_top_bar)) {
            mTopBar.setVisibility(View.GONE);
        }
    }

    private void changeFragment(Fragment newFragment) {
        hideKeyboard();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        boolean firstLaunch = LinphonePreferences.instance().isFirstLaunch();

        /*if (id == R.id.assistant_cancel) {
            hideKeyboard();
            LinphonePreferences.instance().firstLaunchSuccessful();
            if (getResources().getBoolean(R.bool.assistant_cancel_move_to_back)) {
                moveTaskToBack(true);
            } else {
                if (firstLaunch) startActivity(new Intent().setClass(this, LinphoneActivity.class));
                finish();
            }
        } else*/
        if (id == R.id.back) {
            hideKeyboard();
            if (mCurrentFragment == AssistantFragmentsEnum.WELCOME) {
                LinphonePreferences.instance().firstLaunchSuccessful();
                if (getResources().getBoolean(R.bool.assistant_cancel_move_to_back)) {
                    moveTaskToBack(true);
                } else {
                    if (firstLaunch)
                        startActivity(new Intent().setClass(this, LinphoneActivity.class));
                    finish();
                }
            } else {
                onBackPressed();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsLink) {
            return;
        }
        // boolean firstLaunch = LinphonePreferences.instance().isFirstLaunch();
        if (mCurrentFragment == AssistantFragmentsEnum.QRCODE_READER) {
            // displayRemoteProvisioning("");
            displayLoginGeneric();
        } else {
            super.onBackPressed();
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void checkAndRequestAudioPermission() {
        checkAndRequestPermission(
                Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO);
    }

    private void checkAndRequestVideoPermission() {
        checkAndRequestPermission(Manifest.permission.CAMERA, PERMISSIONS_REQUEST_CAMERA);
    }

    private void checkAndRequestPermission(String permission, int result) {
        int permissionGranted = getPackageManager().checkPermission(permission, getPackageName());
        Log.i(
                "[Permission] "
                        + permission
                        + " is "
                        + (permissionGranted == PackageManager.PERMISSION_GRANTED
                                ? "granted"
                                : "denied"));

        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            Log.i("[Permission] Asking for " + permission);
            ActivityCompat.requestPermissions(this, new String[] {permission}, result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NotNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            Log.i(
                    "[Permission] "
                            + permissions[i]
                            + " is "
                            + (grantResults[i] == PackageManager.PERMISSION_GRANTED
                            ? "granted"
                            : "denied"));
            if (permissions[i].equals(Manifest.permission.CAMERA)
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                LinphoneUtils.reloadVideoDevices();
            }
        }

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayQRCodeReader();
                }
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchEchoCancellerCalibration();
                } else {
                    isEchoCalibrationFinished();
                }
                break;
        }
    }

    private void launchEchoCancellerCalibration() {
        int recordAudio =
                getPackageManager()
                        .checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName());
        Log.i(
                "[Permission] Record audio permission is "
                        + (recordAudio == PackageManager.PERMISSION_GRANTED
                                ? "granted"
                                : "denied"));

        if (recordAudio == PackageManager.PERMISSION_GRANTED) {
            EchoCancellerCalibrationFragment fragment = new EchoCancellerCalibrationFragment();
            fragment.enableEcCalibrationResultSending(true);
            changeFragment(fragment);
            mCurrentFragment = AssistantFragmentsEnum.ECHO_CANCELLER_CALIBRATION;
            mBack.setVisibility(View.VISIBLE);
            mBack.setEnabled(false);
        } else {
            checkAndRequestAudioPermission();
        }
    }

    private void configureProxyConfig(AccountCreator accountCreator) {
        Core lc = LinphoneManager.getLc();
        ProxyConfig proxyConfig = lc.createProxyConfig();
        AuthInfo authInfo;

        String identity = proxyConfig.getIdentityAddress().asStringUriOnly();
        if (identity == null || accountCreator.getUsername() == null) {
            LinphoneUtils.displayErrorAlert(getString(R.string.error), this);
            return;
        }
        identity = identity.replace("?", accountCreator.getUsername());
        Address addr = Factory.instance().createAddress(identity);
        addr.setDisplayName(accountCreator.getUsername());
        mAddress = addr;
        proxyConfig.edit();

        proxyConfig.setIdentityAddress(addr);

        if (accountCreator.getPhoneNumber() != null && accountCreator.getPhoneNumber().length() > 0)
            proxyConfig.setDialPrefix(
                    org.linphone.core.Utils.getPrefixFromE164(accountCreator.getPhoneNumber()));

        proxyConfig.done();

        authInfo =
                Factory.instance()
                        .createAuthInfo(
                                accountCreator.getUsername(),
                                null,
                                accountCreator.getPassword(),
                                accountCreator.getHa1(),
                                proxyConfig.getRealm(),
                                proxyConfig.getDomain());

        lc.addProxyConfig(proxyConfig);

        lc.addAuthInfo(authInfo);

        lc.setDefaultProxyConfig(proxyConfig);

        if (LinphonePreferences.instance() != null)
            LinphonePreferences.instance().setPushNotificationEnabled(true);

        if (!mNewAccount) {
            displayRegistrationInProgressDialog();
        }
        mAccountCreated = true;
    }

    public void linphoneLogIn(AccountCreator accountCreator) {
        LinphoneManager.getLc()
                .loadConfigFromXml(LinphoneManager.getInstance().getLinphoneDynamicConfigFile());
        configureProxyConfig(accountCreator);
        // Restore default values for proxy config
        LinphoneManager.getLc()
                .loadConfigFromXml(LinphoneManager.getInstance().getDefaultDynamicConfigFile());
    }

    public void genericLogIn(
            String username,
            String userid,
            String password,
            String displayname,
            String prefix,
            String domain,
            TransportType transport,
            String nethUsername,
            Integer proxyPort) {
        Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (core == null) return;

        AuthInfo authInfo =
                Factory.instance().createAuthInfo(username, userid, password, null, null, domain);
        core.addAuthInfo(authInfo);

        ProxyConfig proxyConfig = core.createProxyConfig();

        String identity = "sip:" + username + "@" + domain;
        Address identityAddr = Factory.instance().createAddress(identity);
        if (identityAddr != null) {
            identityAddr.setDisplayName(displayname);
            proxyConfig.setIdentityAddress(identityAddr);
        }

        String proxy = "<sip:" + domain;
        if (proxyPort != null) {
            proxy += ":" + proxyPort;
        }
        proxy += ";transport=" + transport.name().toLowerCase() + ">";

        proxyConfig.setServerAddr(proxy);
        proxyConfig.setDialPrefix(prefix);

        // setup proxy settings
        core.setMediaEncryption(MediaEncryption.SRTP);
        core.setMediaEncryptionMandatory(proxyPort != null);
        proxyConfig.setExpires(proxyPort != null ? expireIfProxyConfigured : expireIfProxyNotConfigured);

        core.addProxyConfig(proxyConfig);
        core.setDefaultProxyConfig(proxyConfig);

        // [Notificatore] login to Notificatore app.
        FCMNotification.updateRegistrationInfo(
                getApplicationContext(),
                FCMNotification.getNotificatoreUserIdentifier(userid, domain));
        PushNotificationUtils.checkForFCM(this);
        LinphonePreferences.instance().togglePushNotification(true);
        mAccountCreated = true;
        mLoginInProgress = false;
        success();
    }

    private void display(AssistantFragmentsEnum fragment) {
        switch (fragment) {
            case WELCOME:
                displayMenu();
                break;
            case LINPHONE_LOGIN:
                displayLoginLinphone(null, null);
                break;
            case CREATE_ACCOUNT:
                displayCreateAccount();
                break;
                // Introduded to open the login onCreate.
            case LOGIN:
                displayLoginGeneric();
                break;
            default:
                throw new IllegalStateException("Can't handle " + fragment);
        }
    }

    private void displayMenu() {
        mFragment = new WelcomeFragment();
        changeFragment(mFragment);
        country = null;
        mCurrentFragment = AssistantFragmentsEnum.WELCOME;
    }

    public void displayLoginGeneric() {
        mFragment = new LoginFragment();
        changeFragment(mFragment);
        mCurrentFragment = AssistantFragmentsEnum.LOGIN;
    }

    public void displayLoginGeneric(String url) {
        mFragment = new LoginFragment();
        /*
         * We have added url for qrcode provisioning.
         * Remove next three rows to remove that.
         */
        Bundle extra = new Bundle();
        extra.putString("RemoteUrl", url);
        mFragment.setArguments(extra);
        changeFragment(mFragment);
        mCurrentFragment = AssistantFragmentsEnum.LOGIN;
    }

    public void displayLoginLinphone(String username, String password) {
        mFragment = new LinphoneLoginFragment();
        Bundle extras = new Bundle();
        extras.putString("Phone", null);
        extras.putString("Dialcode", null);
        extras.putString("Username", username);
        extras.putString("Password", password);
        mFragment.setArguments(extras);
        changeFragment(mFragment);
        mCurrentFragment = AssistantFragmentsEnum.LINPHONE_LOGIN;
    }

    public void displayCreateAccount() {
        mFragment = new CreateAccountFragment();
        Bundle extra = new Bundle();
        extra.putBoolean("LinkPhoneNumber", mIsLink);
        extra.putBoolean("LinkFromPref", mFromPref);
        mFragment.setArguments(extra);
        changeFragment(mFragment);
        mCurrentFragment = AssistantFragmentsEnum.CREATE_ACCOUNT;
    }

    public void displayRemoteProvisioning(String url) {
        mFragment = new RemoteProvisioningFragment();
        Bundle extra = new Bundle();
        extra.putString("RemoteUrl", url);
        mFragment.setArguments(extra);
        changeFragment(mFragment);
        mCurrentFragment = AssistantFragmentsEnum.REMOTE_PROVISIONING;
    }

    public void displayQRCodeReader() {
        if (getPackageManager().checkPermission(Manifest.permission.CAMERA, getPackageName())
                != PackageManager.PERMISSION_GRANTED) {
            checkAndRequestVideoPermission();
        } else {
            mFragment = new QrCodeFragment();
            changeFragment(mFragment);
            mCurrentFragment = AssistantFragmentsEnum.QRCODE_READER;
        }
    }

    public void displayCountryChooser() {
        mFragment = new CountryListFragment();
        changeFragment(mFragment);
        mLastFragment = mCurrentFragment;
        mCurrentFragment = AssistantFragmentsEnum.COUNTRY_CHOOSER;
    }

    private void launchDownloadCodec() {
        /*if (OpenH264DownloadHelper.isOpenH264DownloadEnabled()) {
            OpenH264DownloadHelper downloadHelper =
                    Factory.instance().createOpenH264DownloadHelper(this);
            if (Version.getCpuAbis().contains("armeabi-v7a")
                    && !Version.getCpuAbis().contains("x86")
                    && !downloadHelper.isCodecFound()) {
                CodecDownloaderFragment codecFragment = new CodecDownloaderFragment();
                changeFragment(codecFragment);
                mCurrentFragment = AssistantFragmentsEnum.DOWNLOAD_CODEC;
                mBack.setEnabled(false);
            } else goToLinphoneActivity();
        } else {
            goToLinphoneActivity();
        }*/
        // There is an issue: https://bugs.linphone.org/view.php?id=6322
        goToLinphoneActivity();
    }

    public void endDownloadCodec() {
        goToLinphoneActivity();
    }

    private void displayRegistrationInProgressDialog() {
        if (LinphoneManager.getLc().isNetworkReachable()) {
            mProgress = ProgressDialog.show(this, null, null);
            Drawable d = new ColorDrawable(ContextCompat.getColor(this, R.color.light_grey_color));
            d.setAlpha(200);
            mProgress
                    .getWindow()
                    .setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
            mProgress.getWindow().setBackgroundDrawable(d);
            mProgress.setContentView(R.layout.wait_layout);
            mProgress.show();
        }
    }

    public void displayRemoteProvisioningInProgressDialog() {
        mRemoteProvisioningInProgress = true;

        mProgress = ProgressDialog.show(this, null, null);
        Drawable d = new ColorDrawable(ContextCompat.getColor(this, R.color.light_grey_color));
        d.setAlpha(200);
        mProgress
                .getWindow()
                .setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
        mProgress.getWindow().setBackgroundDrawable(d);
        mProgress.setContentView(R.layout.wait_layout);
        mProgress.show();
    }

    public void displayNethLoginInProgressDialog() {
        mLoginInProgress = true;
        mProgress = ProgressDialog.show(this, null, null);
        Drawable d = new ColorDrawable(ContextCompat.getColor(this, R.color.light_grey_color));
        d.setAlpha(100);
        mProgress
                .getWindow()
                .setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
        mProgress.getWindow().setBackgroundDrawable(d);
        mProgress.setContentView(R.layout.wait_layout);
        mProgress.show();
    }

    public void displayAssistantConfirm(String username, String password, String email) {
        CreateAccountActivationFragment fragment = new CreateAccountActivationFragment();
        mNewAccount = true;
        Bundle extras = new Bundle();
        extras.putString("Username", username);
        extras.putString("Password", password);
        extras.putString("Email", email);
        fragment.setArguments(extras);
        changeFragment(fragment);

        mCurrentFragment = AssistantFragmentsEnum.CREATE_ACCOUNT_ACTIVATION;
    }

    public void displayAssistantCodeConfirm(
            String username, String phone, String dialcode, boolean recoverAccount) {
        CreateAccountCodeActivationFragment fragment = new CreateAccountCodeActivationFragment();
        mNewAccount = true;
        Bundle extras = new Bundle();
        extras.putString("Username", username);
        extras.putString("Phone", phone);
        extras.putString("Dialcode", dialcode);
        extras.putBoolean("RecoverAccount", recoverAccount);
        extras.putBoolean("LinkAccount", mIsLink);
        fragment.setArguments(extras);
        changeFragment(fragment);

        mCurrentFragment = AssistantFragmentsEnum.CREATE_ACCOUNT_CODE_ACTIVATION;
    }

    public void displayAssistantLinphoneLogin(String phone, String dialcode) {
        LinphoneLoginFragment fragment = new LinphoneLoginFragment();
        mNewAccount = true;
        Bundle extras = new Bundle();
        extras.putString("Phone", phone);
        extras.putString("Dialcode", dialcode);
        fragment.setArguments(extras);
        changeFragment(fragment);

        mCurrentFragment = AssistantFragmentsEnum.LINPHONE_LOGIN;
    }

    /**
     * Use this to close the progress dialog at any moment.
     *
     * @param resId The Resource Id to show.
     */
    public void dismissProgessDialogWithToast(int resId) {
        Toast.makeText(AssistantActivity.instance(), resId, Toast.LENGTH_LONG).show();
        if (mProgress != null) mProgress.dismiss();
    }

    /**
     * Use this to close the progress dialog at any moment.
     *
     * @param message The message to show in a toast.
     */
    public void dismissProgressDialogWithMessage(String message) {
        if (message != null)
            Toast.makeText(AssistantActivity.instance(), message, Toast.LENGTH_LONG).show();
        if (mProgress != null) mProgress.dismiss();
    }

    private void initSideMenu() {
        mSideMenu = findViewById(R.id.side_menu);
        mSideMenuItems = new ArrayList<>();

        mSideMenuItems.add(
                new LinphoneActivity.MenuItem(
                        getResources().getString(R.string.assistant_login),
                        R.drawable.ic_login_drawer));
        if (!getResources().getBoolean(R.bool.hide_settings_from_side_menu)) {
            mSideMenuItems.add(
                    new LinphoneActivity.MenuItem(
                            getResources().getString(R.string.menu_settings),
                            R.drawable.ic_settings));
        }
        if (!getResources().getBoolean(R.bool.hide_recordings_from_side_menu)) {
            mSideMenuItems.add(
                    new LinphoneActivity.MenuItem(
                            getResources().getString(R.string.menu_recordings),
                            R.drawable.ic_recordings));
        }
        mSideMenuItems.add(
                new LinphoneActivity.MenuItem(getResources().getString(R.string.menu_about), R.drawable.ic_about));
        mSideMenuContent = findViewById(R.id.side_menu_content);
        mSideMenuItemList = findViewById(R.id.item_list);
        mMenu = findViewById(R.id.side_menu_button);

        menuAdapter = new LinphoneActivity.MenuAdapter(this, R.layout.side_menu_item_cell, mSideMenuItems);
        mSideMenuItemList.setAdapter(menuAdapter);
        mSideMenuItemList.setOnItemClickListener(
                (adapterView, view, i, l) -> {
                    String selectedItem = mSideMenuItemList.getAdapter().getItem(i).toString();
                    String extra = "";
                    if (selectedItem.equals(getString(R.string.menu_settings))) {
                        extra = "Settings";
                    } else if (selectedItem.equals(getString(R.string.menu_about))) {
                        extra = "About";
                    } else if (selectedItem.equals(getString(R.string.menu_assistant))) {
                        changeFragment(new LoginFragment());
                    } else
                    if (selectedItem.equals(getString(R.string.menu_recordings))) {
                        extra = "Recordings";
                    }
                    if(!extra.isEmpty()) {
                        startActivity(
                                new Intent()
                                        .setClass(this, LinphoneActivity.class)
                                        .putExtra(extra, true));
                    }
                    openOrCloseSideMenu(false);
                });

        mMenu.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (mSideMenu.isDrawerVisible(Gravity.LEFT)) {
                            mSideMenu.closeDrawer(mSideMenuContent);
                        } else {
                            mSideMenu.openDrawer(mSideMenuContent);
                        }
                    }
                });

        mQuitLayout = findViewById(R.id.side_menu_quit);
        mQuitLayout.setOnClickListener(
                view -> quit()
        );
    }

    private void openOrCloseSideMenu(boolean open) {
        if (open) {
            mSideMenu.openDrawer(mSideMenuContent);
        } else {
            mSideMenu.closeDrawer(mSideMenuContent);
        }
    }

    private void quit() {
        finish();
        stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(getString(R.string.sync_account_type));
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void isAccountVerified() {
        Toast.makeText(this, getString(R.string.assistant_account_validated), Toast.LENGTH_LONG)
                .show();
        hideKeyboard();
        success();
    }

    public void isEchoCalibrationFinished() {
        launchDownloadCodec();
    }

    private Dialog createErrorDialog(ProxyConfig proxy, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (message.equals("Forbidden")) {
            message = getString(R.string.assistant_error_bad_credentials);
        }
        builder.setMessage(message)
                .setTitle(proxy.getState().toString())
                .setPositiveButton(
                        getString(R.string.continue_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                success();
                            }
                        })
                .setNegativeButton(
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                LinphoneManager.getLc()
                                        .removeProxyConfig(
                                                LinphoneManager.getLc().getDefaultProxyConfig());
                                LinphonePreferences.instance().resetDefaultProxyConfig();
                                LinphoneManager.getLc().refreshRegisters();
                                dialog.dismiss();
                            }
                        });
        return builder.show();
    }

    public void success() {
        boolean needsEchoCalibration = LinphoneManager.getLc().isEchoCancellerCalibrationRequired();
        if (needsEchoCalibration && mPrefs.isFirstLaunch()) {
            launchEchoCancellerCalibration();
        } else {
            launchDownloadCodec();
        }
    }

    private void goToLinphoneActivity() {
        mPrefs.firstLaunchSuccessful();
        LinphoneManager.getInstance().changeStatusToOnline();
        startActivity(
                new Intent()
                        .setClass(this, LinphoneActivity.class)
                        .putExtra("isNewProxyConfig", true)
                        .putExtra("mustHideLogin", mustHideLogin));
        finish();
    }

    public void setCoreListener() {
        Core lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }
        if (mStatus != null) {
            mStatus.setCoreListener();
        }
    }

    public void restartApplication() {
        mPrefs.firstLaunchSuccessful();

        Intent mStartActivity = new Intent(this, LinphoneLauncherActivity.class);
        PendingIntent mPendingIntent =
                PendingIntent.getActivity(
                        this,
                        (int) System.currentTimeMillis(),
                        mStartActivity,
                        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);

        finish();
        stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(getString(R.string.sync_account_type));
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onIsAccountExist(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {
        if (status.equals(AccountCreator.Status.AccountExistWithAlias)) {
            success();
        } else {
            mIsLink = true;
            displayCreateAccount();
        }
        if (mAccountCreator != null) mAccountCreator.setListener(null);
    }

    @Override
    public void onCreateAccount(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    @Override
    public void onActivateAccount(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    @Override
    public void onLinkAccount(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    @Override
    public void onActivateAlias(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    @Override
    public void onLoginLinphoneAccount(@NonNull AccountCreator creator, AccountCreator.Status status, @Nullable String response) {

    }

    @Override
    public void onIsAccountActivated(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    @Override
    public void onRecoverAccount(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    @Override
    public void onIsAccountLinked(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    @Override
    public void onIsAliasUsed(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    @Override
    public void onUpdateAccount(
            AccountCreator accountCreator, AccountCreator.Status status, String resp) {}

    public CountryListAdapter getCountryListAdapter() {
        return mCountryListAdapter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mCurrentFragment == AssistantFragmentsEnum.QRCODE_READER) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    /**
     * This class reads a JSON file containing Country-specific phone number description, and allows
     * to present them into a ListView
     */
    public class CountryListAdapter extends BaseAdapter implements Filterable {

        private LayoutInflater mInflater;
        private final DialPlan[] allCountries;
        private List<DialPlan> filteredCountries;
        private final Context context;

        CountryListAdapter(Context ctx) {
            context = ctx;
            allCountries = Factory.instance().getDialPlans();
            filteredCountries = new ArrayList<>(Arrays.asList(allCountries));
        }

        public void setInflater(LayoutInflater inf) {
            mInflater = inf;
        }

        public DialPlan getCountryFromCountryCode(String countryCode) {
            countryCode = (countryCode.startsWith("+")) ? countryCode.substring(1) : countryCode;
            for (DialPlan c : allCountries) {
                if (c.getCountryCallingCode().compareTo(countryCode) == 0) return c;
            }
            return null;
        }

        @Override
        public int getCount() {
            return filteredCountries.size();
        }

        @Override
        public DialPlan getItem(int position) {
            return filteredCountries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.country_cell, parent, false);
            }

            DialPlan c = filteredCountries.get(position);

            TextView name = view.findViewById(R.id.country_name);
            name.setText(c.getCountry());

            TextView dial_code = view.findViewById(R.id.country_prefix);
            if (context != null)
                dial_code.setText(
                        String.format(
                                context.getString(R.string.country_code),
                                c.getCountryCallingCode()));

            view.setTag(c);
            return view;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    ArrayList<DialPlan> filteredCountries = new ArrayList<>();
                    for (DialPlan c : allCountries) {
                        if (c.getCountry().toLowerCase().contains(constraint)
                                || c.getCountryCallingCode().contains(constraint)) {
                            filteredCountries.add(c);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filteredCountries;
                    return filterResults;
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredCountries = (List<DialPlan>) results.values;
                    CountryListAdapter.this.notifyDataSetChanged();
                }
            };
        }
    }
}

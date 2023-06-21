package org.linphone.assistant;
/*
LoginFragment.java
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

import static it.nethesis.webservices.RetrofitGenerator.calculateRFC2104HMAC;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import it.nethesis.models.Extension;
import it.nethesis.models.LoginCredentials;
import it.nethesis.models.NethUser;
import it.nethesis.webservices.AuthenticationRestAPI;
import it.nethesis.webservices.RetrofitGenerator;
import it.nethesis.webservices.UserRestAPI;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.linphone.R;
import org.linphone.core.TransportType;
import org.linphone.utils.SharedPreferencesManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment implements OnClickListener, TextWatcher {
    private EditText mLogin, mPassword, mDomain;
    private Button mApply;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.assistant_login, container, false);

        mLogin = view.findViewById(R.id.assistant_username);
        mLogin.addTextChangedListener(this);
        mPassword = view.findViewById(R.id.assistant_password);
        mPassword.addTextChangedListener(this);
        mDomain = view.findViewById(R.id.assistant_domain);
        // Set this line while testing. mDomain.setText(R.string.neth_test_domain);
        mDomain.addTextChangedListener(this);
        mApply = view.findViewById(R.id.assistant_apply);
        mApply.setEnabled(false);
        mApply.setOnClickListener(this);
        Button mQrCode = view.findViewById(R.id.launch_qrcode_scan);
        mQrCode.setOnClickListener(this);

        if (getArguments() != null) {
            String toSplit = getArguments().getString("RemoteUrl");
            if (toSplit == null) {
                Log.w("QR_CODE_PROVISIONING", "No QrCode url provided.");
                return view;
            }

            // If i can't split the string I return a message error.
            String[] separated = toSplit.split(";");
            if (separated.length != 3) {
                Log.e(
                        "LOGIN_ERROR",
                        getResources().getString(R.string.neth_login_qrcode_split_error));
                Toast.makeText(
                                AssistantActivity.instance(),
                                R.string.neth_login_qrcode_split_error,
                                Toast.LENGTH_LONG)
                        .show();
                return view;
            }

            // performNethLogin(separated[0], separated[1], separated[2]); Before the AuthToken.
            manageLoginResponse(String.format("%s:%s", separated[0], separated[1]), separated[2]);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.assistant_apply) {
            if (mLogin.getText() == null
                    || mLogin.length() == 0
                    || mPassword.getText() == null
                    || mPassword.length() == 0
                    || mDomain.getText() == null
                    || mDomain.length() == 0) {
                // Here I show an input error.
                Toast.makeText(
                                getActivity(),
                                getString(R.string.first_launch_no_login_password),
                                Toast.LENGTH_LONG)
                        .show();
                return;
            }

            // Here I've input correctly all fields.
            if (mDomain.getText().toString().compareTo(getString(R.string.default_domain)) == 0) {
                AssistantActivity.instance()
                        .displayLoginLinphone(
                                mLogin.getText().toString(), mPassword.getText().toString());
            } else {
                performNethLogin(
                        mLogin.getText().toString(),
                        mPassword.getText().toString(),
                        mDomain.getText().toString());
                /*
                 * We have forced the user to use TLS instead other protocols.
                 * We don't need the userid and displayname: removed.
                 */
            }
        }

        if (id == R.id.launch_qrcode_scan) {
            AssistantActivity.instance().displayQRCodeReader();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mApply.setEnabled(
                !mLogin.getText().toString().isEmpty()
                        && !mPassword.getText().toString().isEmpty()
                        && !mDomain.getText().toString().isEmpty());
    }

    @Override
    public void afterTextChanged(Editable s) {}

    /**
     * Start of the new Neth Login Procedure. Perform the first api call.
     *
     * @param username Username.
     * @param password Password.
     * @param domain Domain.
     */
    private void performNethLogin(
            final String username, final String password, final String domain) {
        AssistantActivity.instance().displayNethLoginInProgressDialog();
        // Enqueue the login api call.
        AuthenticationRestAPI restAPIClass =
                RetrofitGenerator.createService(AuthenticationRestAPI.class, domain);
        LoginCredentials credentials = new LoginCredentials(username, password);
        Call<String> call = restAPIClass.login(credentials);
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        // If I've not or I've a not valid response header, I'll exit.
                        if (response == null || response.headers() == null) {
                            AssistantActivity.instance()
                                    .dismissProgessDialogWithToast(
                                            R.string.neth_login_wrong_credentials);
                            return;
                        }

                        // Now I'll enqueue the me api call, to get the extension.
                        String authHeader = response.headers().get("www-authenticate");
                        if (authHeader == null) {
                            AssistantActivity.instance()
                                    .dismissProgessDialogWithToast(
                                            R.string.neth_login_missing_authentication_header);
                            return;
                        }

                        String authToken;
                        try {
                            authToken =
                                    String.format(
                                            "%s:%s",
                                            username,
                                            calculateRFC2104HMAC(
                                                    String.format(
                                                            "%s:%s:%s",
                                                            username,
                                                            password,
                                                            authHeader.substring(7)),
                                                    password));
                        } catch (Exception e) {
                            AssistantActivity.instance()
                                    .dismissProgessDialogWithToast(
                                            R.string.neth_login_missing_authentication_header);
                            return;
                        }

                        manageLoginResponse(authToken, domain);
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        manageLoginFailure();
                    }
                });
    }

    /**
     * Manage the first result and perform the second api call.
     *
     * @param authToken Authentication Token received and calculated.
     * @param domain Domain put before.
     */
    private void manageLoginResponse(final String authToken, final String domain) {
        // Save the authToken and domain in the SharedPreferences
        SharedPreferencesManager.setAuthtoken(getActivity().getApplicationContext(), authToken);
        SharedPreferencesManager.setDomain(getActivity().getApplicationContext(), domain);

        UserRestAPI userRestAPI = RetrofitGenerator.createService(UserRestAPI.class, domain);
        Call<NethUser> getMeCall = userRestAPI.getMe(authToken);
        getMeCall.enqueue(
                new Callback<NethUser>() {
                    @Override
                    public void onResponse(Call<NethUser> call, Response<NethUser> response) {
                        NethUser nethUser = response.body();
                        if (nethUser == null
                                || nethUser.endpoints == null
                                || nethUser.endpoints.extension == null) {
                            AssistantActivity.instance()
                                    .dismissProgessDialogWithToast(
                                            R.string.neth_login_missing_neth_user);
                            return;
                        }

                        manageNethUserIntern(nethUser, domain);
                    }

                    @Override
                    public void onFailure(Call<NethUser> call, Throwable t) {
                        manageNethUserInternFailure();
                    }
                });
    }

    /**
     * Manage the second result and perform the SIP login.
     *
     * @param nethUser Neth user from first call.
     * @param domain Domain putted before.
     */
    private void manageNethUserIntern(@NotNull final NethUser nethUser, final String domain) {
        List<Extension> extensions = nethUser.endpoints.extension;
        for (Extension e : extensions) {
            if (e.type.equals("mobile")) { // Before was e.type.equals("webrtc");
                AssistantActivity.instance()
                        .genericLogIn(
                                e.id,
                                e.username,
                                e.secret,
                                nethUser.name,
                                null,
                                domain,
                                TransportType.Tls,
                                nethUser.username,
                                e.proxyPort);
                // I do login with only one extension.
                SharedPreferencesManager.setUsername(
                        getActivity().getApplicationContext(), nethUser.username);
                try {
                    if (nethUser.endpoints.mainextension.get(0) != null){
                        String mainExt = nethUser.endpoints.mainextension.get(0).id;
                        SharedPreferencesManager.setMainExtension(
                                getActivity().getApplicationContext(), mainExt
                        );
                    }
                } catch (Exception ignored) { /* ... */ }
                return;
            }
        }

        // I haven't found any extension.
        AssistantActivity.instance()
                .dismissProgessDialogWithToast(R.string.neth_login_missing_neth_extension);
    }

    /** Manage the error from the second api call. */
    private void manageNethUserInternFailure() {
        AssistantActivity.instance()
                .dismissProgessDialogWithToast(R.string.neth_login_2_call_failed);
    }

    /** Manage the error from the first api call. */
    private void manageLoginFailure() {
        AssistantActivity.instance()
                .dismissProgessDialogWithToast(R.string.neth_login_1_call_failed);
    }
}

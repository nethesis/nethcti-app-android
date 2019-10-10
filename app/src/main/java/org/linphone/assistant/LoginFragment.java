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

import static org.linphone.utils.webservices.RetrofitGenerator.calculateRFC2104HMAC;

import android.app.Fragment;
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
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.linphone.R;
import org.linphone.core.TransportType;
import org.linphone.models.Extension;
import org.linphone.models.LoginCredentials;
import org.linphone.models.NethUser;
import org.linphone.utils.webservices.AuthenticationRestAPI;
import org.linphone.utils.webservices.RetrofitGenerator;
import org.linphone.utils.webservices.UserRestAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment implements OnClickListener, TextWatcher {
    private EditText mLogin, mUserid, mPassword, mDomain, mDisplayName;
    private RadioGroup mTransports;
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
        mDomain.setText(R.string.neth_test_domain);
        mDomain.addTextChangedListener(this);
        mApply = view.findViewById(R.id.assistant_apply);
        mApply.setEnabled(false);
        mApply.setOnClickListener(this);
        Button mQrCode = view.findViewById(R.id.lauch_qrcode_mahahahah);
        mQrCode.setOnClickListener(this);

        if (getArguments() != null) {
            String toSplit = getArguments().getString("RemoteUrl");
            if (toSplit == null) {
                Log.w("QR_CODE_PROVISIONING", "No QrCode url provided.");
                return view;
            }

            String[] separated = toSplit.split(";");
            performNethLogin(separated[0], separated[1], separated[2]);
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

        if (id == R.id.lauch_qrcode_mahahahah) {
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

    private void performNethLogin(
            final String username, final String password, final String domain) {
        // Enqueue the login api call.
        AuthenticationRestAPI restAPIClass =
                RetrofitGenerator.createService(AuthenticationRestAPI.class);
        Call<String> call = restAPIClass.login(new LoginCredentials(username, password));
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        // If I've not or I've a not valid response header, I'll exit.
                        if (response == null || response.headers() == null) {
                            Toast.makeText(
                                            AssistantActivity.instance(),
                                            R.string.neth_login_wrong_credentials,
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        // Now I'll enqueue the me api call, to get the extension.
                        String authHeader = response.headers().get("www-authenticate");
                        if (authHeader == null) {
                            Toast.makeText(
                                            AssistantActivity.instance(),
                                            R.string.neth_login_missing_authentication_header,
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        final String digest = authHeader.substring(7);
                        UserRestAPI userRestAPI =
                                RetrofitGenerator.createService(UserRestAPI.class);
                        String sha1;
                        try {
                            sha1 =
                                    String.format(
                                            "%s:%s",
                                            username,
                                            calculateRFC2104HMAC(
                                                    String.format(
                                                            "%s:%s:%s", username, password, digest),
                                                    password));
                        } catch (NoSuchAlgorithmException e) {
                            Toast.makeText(
                                            AssistantActivity.instance(),
                                            R.string.neth_login_missing_crypt_algorithm,
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        } catch (InvalidKeyException e) {
                            Toast.makeText(
                                            AssistantActivity.instance(),
                                            R.string.neth_login_missing_crypt_key,
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        } catch (UnsupportedEncodingException e) {
                            Toast.makeText(
                                            AssistantActivity.instance(),
                                            R.string.neth_login_missing_crypt_encoding,
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        Call<NethUser> getMeCall = userRestAPI.getMe(sha1);
                        getMeCall.enqueue(
                                new Callback<NethUser>() {
                                    @Override
                                    public void onResponse(
                                            Call<NethUser> call, Response<NethUser> response) {
                                        NethUser nethUser = response.body();
                                        if (nethUser == null
                                                || nethUser.endpoints == null
                                                || nethUser.endpoints.extension == null) {
                                            Toast.makeText(
                                                            AssistantActivity.instance(),
                                                            R.string.neth_login_missing_neth_user,
                                                            Toast.LENGTH_LONG)
                                                    .show();
                                            return;
                                        }

                                        List<Extension> extensions = nethUser.endpoints.extension;
                                        for (Extension e : extensions) {
                                            if (e.type.equals("webrtc")) {
                                                AssistantActivity.instance()
                                                        .genericLogIn(
                                                                e.id,
                                                                null,
                                                                e.secret,
                                                                e.username,
                                                                null,
                                                                domain,
                                                                TransportType.Tls);
                                                // I do login with only one extension.
                                                return;
                                            }
                                        }

                                        // I haven't found any extension.
                                        Toast.makeText(
                                                        AssistantActivity.instance(),
                                                        R.string.neth_login_missing_neth_extension,
                                                        Toast.LENGTH_LONG)
                                                .show();
                                    }

                                    @Override
                                    public void onFailure(Call<NethUser> call, Throwable t) {
                                        Toast.makeText(
                                                        AssistantActivity.instance(),
                                                        R.string.neth_login_2_call_failed,
                                                        Toast.LENGTH_LONG)
                                                .show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(
                                        AssistantActivity.instance(),
                                        R.string.neth_login_1_call_failed,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}

package org.linphone.fragments;

/*
DialerFragment.java
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

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import it.nethesis.utils.CallTransferManager;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.core.Core;
import org.linphone.views.AddressAware;
import org.linphone.views.AddressText;
import org.linphone.views.CallButton;
import org.linphone.views.EraseButton;

public class DialerFragment extends Fragment {
    private static DialerFragment sInstance;

    private AddressAware mNumpad;
    private AddressText mAddress;
    private CallButton mCall;
    private MaterialButton mAddContact;
    private OnClickListener mAddContactListener, mCancelListener, mTransferListener;
    private String number;

    /** @return null if not ready yet */
    public static DialerFragment instance() {
        return sInstance;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialer, container, false);

        mAddress = view.findViewById(R.id.address);
        mAddress.setDialerFragment(this);

        EraseButton erase = view.findViewById(R.id.erase);
        erase.setAddressWidget(mAddress);

        mCall = view.findViewById(R.id.call);
        mCall.setAddressWidget(mAddress);
        if (LinphoneActivity.isInstanciated()
                && LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null
                && LinphoneManager.getLcIfManagerNotDestroyedOrNull().getCallsNb() > 0) {
            if (CallTransferManager.instance().ismCallTransfer()) {
                mCall.setIconResource(R.drawable.call_transfer);
            } else {
                mCall.setIconResource(R.drawable.call_add);
            }
        } else {
            if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null
                    && LinphoneManager.getLcIfManagerNotDestroyedOrNull()
                            .getVideoActivationPolicy()
                            .getAutomaticallyInitiate()) {
                mCall.setIconResource(R.drawable.ic_phone);
            } else {
                mCall.setIconResource(R.drawable.ic_phone);
            }
        }

        mNumpad = view.findViewById(R.id.numpad);
        if (mNumpad != null) {
            mNumpad.setAddressWidget(mAddress);
        }

        mAddContact = view.findViewById(R.id.add_contact);
        mAddContact.setEnabled(
                !(LinphoneActivity.isInstanciated()
                        && LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null
                        && LinphoneManager.getLc().getCallsNb() > 0));

        mAddContactListener =
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinphoneActivity.instance()
                                .displayContactsForEdition(mAddress.getText().toString());
                    }
                };
        mCancelListener =
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinphoneActivity.instance()
                                .resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
                    }
                };
        mTransferListener =
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Core lc = LinphoneManager.getLc();
                        if (lc.getCurrentCall() == null) {
                            return;
                        }

                        if (mAddress.getText().length() > 0) {
                            CallTransferManager.instance()
                                    .setmTransferCallId(
                                            LinphoneManager.getLc()
                                                    .getCurrentCall()
                                                    .getRemoteAddress());
                            LinphoneManager.getInstance().newOutgoingCall(mAddress);
                        }
                    }
                };

        resetLayout();

        if (getArguments() != null) {
            String number = getArguments().getString("SipUri");
            String displayName = getArguments().getString("DisplayName");
            mAddress.setText(number);
            if (displayName != null) {
                mAddress.setDisplayedName(displayName);
            }
        }

        if (getArguments() != null) {
            String number = getArguments().getString("tel");
            if (number != null) {
                mAddress.setText(number);
                mAddress.setSelection(number.length());
            }
        }

        sInstance = this;

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        sInstance = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        sInstance = this;

        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().selectMenu(FragmentsAvailable.DIALER);
            LinphoneActivity.instance().updateDialerFragment();
            LinphoneActivity.instance().showStatusBar();
        }

        boolean isOrientationLandscape =
                getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_LANDSCAPE;
        if (isOrientationLandscape && !getResources().getBoolean(R.bool.isTablet)) {
            ((LinearLayout) mNumpad).setVisibility(View.GONE);
        } else {
            ((LinearLayout) mNumpad).setVisibility(View.VISIBLE);
        }

        resetLayout();

        String addressWaitingToBeCalled = LinphoneActivity.instance().addressWaitingToBeCalled;
        if (addressWaitingToBeCalled != null) {
            mAddress.setText(addressWaitingToBeCalled);
            // if (!LinphoneActivity.instance().isCallTransfer()
            if (!CallTransferManager.instance().ismCallTransfer()
                    && getResources()
                            .getBoolean(R.bool.automatically_start_intercepted_outgoing_gsm_call)) {
                newOutgoingCall(addressWaitingToBeCalled);
            }
            LinphoneActivity.instance().addressWaitingToBeCalled = null;
        }
    }

    public void resetLayout() {
        if (!LinphoneActivity.isInstanciated()) {
            return;
        }
        Core lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc == null) {
            return;
        }

        if (lc.getCallsNb() > 0) {
            if (CallTransferManager.instance().ismCallTransfer()) {
                mCall.setIconResource(R.drawable.ic_phone_out);
                mCall.setExternalClickListener(mTransferListener);
            } else {
                mCall.setIconResource(R.drawable.ic_phone_add);
                mCall.resetClickListener();
            }
            mAddContact.setEnabled(true);
            mAddContact.setIconResource(R.drawable.ic_phone_in);
            mAddContact.setOnClickListener(mCancelListener);
        } else {
            mCall.resetClickListener();
            if (LinphoneManager.getLc().getVideoActivationPolicy().getAutomaticallyInitiate()) {
                mCall.setIconResource(R.drawable.ic_phone);
            } else {
                mCall.setIconResource(R.drawable.ic_phone);
            }
            mAddContact.setEnabled(false);
            mAddContact.setIconResource(R.drawable.ic_add_user);
            mAddContact.setOnClickListener(mAddContactListener);
            enableDisableAddContact();
        }
    }

    public void enableDisableAddContact() {
        mAddContact.setEnabled(
                LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null
                                && LinphoneManager.getLc().getCallsNb() > 0
                        || !mAddress.getText().toString().equals(""));
    }

    public void displayTextInAddressBar(String numberOrSipAddress) {
        mAddress.setText(numberOrSipAddress);
    }

    public void newOutgoingCall(String numberOrSipAddress) {
        displayTextInAddressBar(numberOrSipAddress);
        LinphoneManager.getInstance().newOutgoingCall(mAddress);
    }
}

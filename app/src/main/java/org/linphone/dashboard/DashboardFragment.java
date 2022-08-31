package org.linphone.dashboard;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.button.MaterialButton;

import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.contacts.ContactsManager;
import org.linphone.contacts.LinphoneContact;
import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.fragments.FragmentsAvailable;
import org.linphone.settings.LinphonePreferences;
import org.linphone.utils.LinphoneUtils;
import org.linphone.views.ContactAvatar;

import java.util.Arrays;
import java.util.List;

public class DashboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dashboard, container, false);

        setButtonListener((MaterialButton) view.findViewById(R.id.dialer_btn));
        setButtonListener((MaterialButton) view.findViewById(R.id.history_btn));
        setButtonListener((MaterialButton) view.findViewById(R.id.contacts_btn));
        setButtonListener((MaterialButton) view.findViewById(R.id.presence_btn));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        View card1 = view.findViewById(R.id.card1);
        View card2 = view.findViewById(R.id.card2);
        List<CallLog> calls = Arrays.asList(LinphoneManager.getLc().getCallLogs());
        /* Used to make Linphone logic of fragment transactions */
        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().setCurrentFragment(FragmentsAvailable.DASHBOARD);

            LinphoneActivity.instance().hideTabBar();
            LinphoneActivity.instance().hideTopBar();
        }
        /* Manage card visibility */
        switch (calls.size()) {
            case 0:
                card1.setVisibility(View.GONE);
                card2.setVisibility(View.GONE);
                setCardListeners(card1, null);
                setCardListeners(card2, null);
                break;
            case 1:
                card1.setVisibility(View.VISIBLE);
                card2.setVisibility(View.GONE);
                updateCardWithContactInfo(card1, calls.get(0));
                setCardListeners(card1, calls.get(0));
                setCardListeners(card2, null);
                break;
            default:
                card1.setVisibility(View.VISIBLE);
                card2.setVisibility(View.VISIBLE);
                updateCardWithContactInfo(card1, calls.get(0));
                updateCardWithContactInfo(card2, calls.get(1));
                setCardListeners(card1, calls.get(0));
                setCardListeners(card2, calls.get(1));
        }
    }

    private void setCardListeners(View card, final CallLog call) {
        if (call == null) {
            card.setOnClickListener(null);
            card.findViewById(R.id.call).setOnClickListener(null);
        } else {
            /* Set Card Click Listener */
            card.setOnClickListener( v -> {
                Address address;
                if (call.getDir() == Call.Dir.Incoming) {
                    address = call.getFromAddress();
                } else {
                    address = call.getToAddress();
                }

                LinphoneActivity.instance().displayHistoryDetail(address.asString(), call);
            });
            /* Set Call Button Listener */
            card.findViewById(R.id.call)
                    .setOnClickListener(v -> {
                        Address address;
                        if (call.getDir() == Call.Dir.Incoming) {
                            address = call.getFromAddress();
                        } else {
                            address = call.getToAddress();
                        }
                        int accountIndex =
                                LinphonePreferences.instance().getDefaultAccountIndex();
                        address.setDomain(
                                LinphonePreferences.instance()
                                        .getAccountDomain(accountIndex));
                        LinphoneActivity.instance()
                                .setAddresGoToDialerAndCall(
                                        address.asStringUriOnly(),
                                        address.getDisplayName());
                    });
        }
    }

    private void updateCardWithContactInfo(View card1, CallLog callLog) {
        View avatar = card1.findViewById(R.id.avatarImg);
        AppCompatTextView contactName = card1.findViewById(R.id.contact_name_tw);
        //AppCompatTextView contactAddress = card1.findViewById(R.id.contact_address_tw);
        ImageView callStateIcon = card1.findViewById(R.id.call_state_icon);

        Address address;
        if (callLog.getDir() == Call.Dir.Incoming) {
            address = callLog.getFromAddress();
            if (callLog.getStatus() == Call.Status.Missed) {
                callStateIcon.setImageResource(R.drawable.ic_call_missed);
            } else {
                callStateIcon.setImageResource(R.drawable.ic_call_in);
            }
        } else {
            address = callLog.getToAddress();
            callStateIcon.setImageResource(R.drawable.ic_call_out);
        }
        LinphoneContact c = ContactsManager.getInstance().findContactFromAddress(address);
        String displayName = null;
        final String sipUri = (address != null) ? address.asString() : "";

        if (c != null) {
            displayName = c.getFullName();
            ContactAvatar.displayAvatar(c, avatar);
        }
        if (displayName == null) {
            contactName.setText(LinphoneUtils.getAddressDisplayName(sipUri));
            ContactAvatar.displayAvatar(LinphoneUtils.getAddressDisplayName(sipUri), avatar);
        } else {
            contactName.setText(displayName);
            ContactAvatar.displayAvatar(displayName, avatar);
        }
        //contactAddress.setText(LinphoneUtils.getDisplayableAddress(address));
    }

    @SuppressLint("NonConstantResourceId")
    private void setButtonListener(final MaterialButton button) {
        button.setOnClickListener(
                view -> {
                    switch (button.getId()) {
                        case R.id.dialer_btn:
                            LinphoneActivity.instance().displayDialer();
                            break;
                        case R.id.history_btn:
                            LinphoneActivity.instance().displayHistory();
                            break;
                        case R.id.contacts_btn:
                            LinphoneActivity.instance().displayContacts(false);
                            break;
                        case R.id.presence_btn:
                            LinphoneActivity.instance().displayUsersByPresence();
                            break;
                    }
                });
    }
}

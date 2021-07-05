package org.linphone.dashboard;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import org.linphone.LinphoneActivity;
import org.linphone.R;

public class DashboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dashboard, container, false);

        setButtonListener((MaterialButton) view.findViewById(R.id.dialer_btn));
        setButtonListener((MaterialButton) view.findViewById(R.id.history_btn));
        setButtonListener((MaterialButton) view.findViewById(R.id.contacts_btn));
        setButtonListener((MaterialButton) view.findViewById(R.id.setting_btn));

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    private void setButtonListener(final MaterialButton button) {

        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
                            case R.id.setting_btn:
                                LinphoneActivity.instance().displaySettings();
                                break;
                        }
                    }
                });
    }
}

package org.linphone.presence;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.linphone.R;

public class UsersByPresenceFragment extends Fragment {

    private RecyclerView recyclerContactsPresence = null;

    public static UsersByPresenceFragment newInstance() {
        UsersByPresenceFragment fragment = new UsersByPresenceFragment();
        //fragment.project_id = project_id;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_by_presence, container, false);
        recyclerContactsPresence = view.findViewById(R.id.rclr_contacts_presence);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO - Richiedere la lista degli utenti!
        //TODO - Bottom Dialog con suddivizione gruppi

    }
}

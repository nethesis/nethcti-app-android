package org.linphone.presence;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.linphone.R;
import org.linphone.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.HashMap;

import it.nethesis.models.presence.PresenceUser;
import it.nethesis.webservices.PresenceRestAPI;
import it.nethesis.webservices.RetrofitGenerator;
import it.nethesis.webservices.UserRestAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersByPresenceFragment extends Fragment implements Callback<HashMap<String, PresenceUser>> {

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

        String authToken = SharedPreferencesManager.getAuthtoken(getContext());
        String domain = SharedPreferencesManager.getDomain(getContext());

        PresenceRestAPI userRestAPI = RetrofitGenerator.createService(PresenceRestAPI.class, domain);
        userRestAPI.getAll(authToken).enqueue(this);

        //TODO - Richiedere la lista degli utenti!
        //TODO - Bottom Dialog con suddivizione gruppi

    }

    @Override
    public void onResponse(Call<HashMap<String, PresenceUser>> call, Response<HashMap<String, PresenceUser>> response) {
        if (response.body() == null) return;
        ArrayList userList = new ArrayList(response.body().values());
        ContactPresenceListAdapter adapter = new ContactPresenceListAdapter(userList);
    }

    @Override
    public void onFailure(Call<HashMap<String, PresenceUser>> call, Throwable t) {
        Log.e("", "");
    }
}

package org.linphone.presence;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import org.linphone.R;
import org.linphone.utils.SharedPreferencesManager;
import java.util.ArrayList;
import java.util.HashMap;
import it.nethesis.models.presence.PresenceUser;
import it.nethesis.webservices.PresenceRestAPI;
import it.nethesis.webservices.RetrofitGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersByPresenceFragment extends Fragment implements Callback<HashMap<String, PresenceUser>>, View.OnClickListener {

    private RecyclerView recyclerContactsPresence = null;
    private ProgressBar progressPresenceList = null;

    private MaterialButton btnGroup = null;
    private MaterialButton btnFavourites = null;

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
        progressPresenceList = view.findViewById(R.id.pgrss_presence_list);
        btnGroup = view.findViewById(R.id.btn_group);
        btnFavourites = view.findViewById(R.id.btn_favourites);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String authToken = SharedPreferencesManager.getAuthtoken(getContext());
        String domain = SharedPreferencesManager.getDomain(getContext());

        PresenceRestAPI userRestAPI = RetrofitGenerator.createService(PresenceRestAPI.class, domain);
        userRestAPI.getAll(authToken).enqueue(this);

        btnGroup.setOnClickListener(this);
        btnFavourites.setOnClickListener(this);

    }

    @Override
    public void onResponse(Call<HashMap<String, PresenceUser>> call, Response<HashMap<String, PresenceUser>> response) {
        if (response.body() == null) return;
        ArrayList userList = new ArrayList(response.body().values());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        ContactPresenceListAdapter adapter = new ContactPresenceListAdapter(userList);

        recyclerContactsPresence.setLayoutManager(layoutManager);
        recyclerContactsPresence.setAdapter(adapter);

    }

    @Override
    public void onFailure(Call<HashMap<String, PresenceUser>> call, Throwable t) {
        Log.e("", "");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_group) showGroupSheetDialog();
        else if (v.getId() == R.id.btn_group) showFavouritesSheetDialog();
    }

    public void showGroupSheetDialog() {
        PresenceGroupsDialog groupDialog = new PresenceGroupsDialog(getContext());
        groupDialog.show();

    }

    public void showFavouritesSheetDialog() {

    }

}

package org.linphone.presence;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import org.linphone.R;
import org.linphone.interfaces.OnNethGroupSelected;
import org.linphone.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.HashMap;

import it.nethesis.models.NethPermissionWithOpGroups;
import it.nethesis.models.NethUser;
import it.nethesis.models.OpGroupsUsers;
import it.nethesis.webservices.PresenceRestAPI;
import it.nethesis.webservices.RetrofitGenerator;
import it.nethesis.webservices.UserRestAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PresenceGroupsDialogActivity extends AppCompatActivity implements OnNethGroupSelected,
        View.OnClickListener, Callback<NethUser>, SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swpGroups;
    private ProgressBar progressBar;
    private RecyclerView _groupRecycler;
    private TextView _txtAlertGroups;

    private NethPermissionWithOpGroups userWithGroupsSelected;

    private PresenceRestAPI _presenceRestAPI;
    private UserRestAPI _userRestAPI;
    private NethUser _nethUser;
    private HashMap<String, OpGroupsUsers> _presenceGroups = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presence_groups_dialog);

        initData();
        initUi();
        initApi();

        getMeRequest();
        progressBar.setVisibility(VISIBLE);

        setResult(RESULT_CANCELED);

    }

    private void initData() {
        userWithGroupsSelected = NethPermissionWithOpGroups.restoreGroupUser(
                this,
                NethPermissionWithOpGroups.getGroupsUserFile(this)
        );
    }

    private void initUi() {
        progressBar = findViewById(R.id.progress_bar);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        _groupRecycler = findViewById(R.id.recycler_groups);
        _groupRecycler.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        _groupRecycler.setLayoutManager(layoutManager);

        _txtAlertGroups = findViewById(R.id.txt_alert_groups);
        TextView txtBtnEnd = findViewById(R.id.txt_btn_end);
        txtBtnEnd.setOnClickListener(this);

        swpGroups = findViewById(R.id.swp_groups);
        swpGroups.setOnRefreshListener(this);

    }

    private void initApi() {
        String domain = SharedPreferencesManager.getDomain(this);
        _presenceRestAPI = RetrofitGenerator.createService(PresenceRestAPI.class, domain);
        _userRestAPI = RetrofitGenerator.createService(UserRestAPI.class, domain);
    }

    private void getMeRequest() {
        _userRestAPI.getMe(SharedPreferencesManager.getAuthtoken(this))
                .enqueue(this);
    }

    @Override public void onRefresh() { getMeRequest(); }

    @Override
    public void onResponse(Call<NethUser> call, Response<NethUser> response) {
        switch (response.code()) {
            case 200:
                setUiStatusOk();
                _nethUser = response.body();

                if (_nethUser == null)
                    setUiStatusFailureWithMessage(getString(R.string.neth_something_wrong));
                else
                    _presenceRestAPI.getAllPresenceGroups(
                            SharedPreferencesManager.getAuthtoken(this)
                    ).enqueue(groupResponse);
                break;
            case 401:
                setUiStatusFailureWithMessage(getString(R.string.neth_session_expired));
                break;
            default:
                setUiStatusFailureWithMessage(getString(R.string.neth_something_wrong));
                break;
        }
    }

    @Override
    public void onFailure(Call<NethUser> call, Throwable t) {
        setUiStatusFailureWithMessage(getString(R.string.neth_something_wrong));
    }

    // scarico la lista dei gruppi
    private final Callback<HashMap<String, OpGroupsUsers>> groupResponse =
            new Callback<HashMap<String, OpGroupsUsers>>() {
                @Override
                public void onResponse(
                        Call<HashMap<String, OpGroupsUsers>> call,
                        Response<HashMap<String, OpGroupsUsers>> response
                ) {
                    switch (response.code()) {
                        case 200:
                            setUiStatusOk();
                            _presenceGroups = response.body();

                            ArrayList<NethPermissionWithOpGroups> assigned = NethPermissionWithOpGroups.getNethsWithAssignedGroup(
                                    _nethUser.getNethPermissions(),
                                    _presenceGroups
                            );
                            NethPermissionWithOpGroups.sortListOfNethPermissionWithOpGroups(assigned);

                            if (assigned.isEmpty()) showAlertMessage(getString(R.string.neth_no_groups));
                            else setUiGroups(assigned);

                            break;
                        case 401:
                            showAlertMessage(getString(R.string.neth_session_expired));
                            break;
                        default:
                            showAlertMessage(getString(R.string.neth_something_wrong));
                            break;
                    }
                }

                @Override
                public void onFailure(
                        Call<HashMap<String, OpGroupsUsers>> call,
                        Throwable t
                ) {
                    setUiStatusFailureWithMessage(getString(R.string.neth_something_wrong));
                }
            };

    private void setUiGroups(ArrayList<NethPermissionWithOpGroups> assigned) {
        GroupListAdapter adapter = new GroupListAdapter(assigned, userWithGroupsSelected,this);
        _groupRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNethGroupSelected(NethPermissionWithOpGroups userWithGroups) {
        this.userWithGroupsSelected = userWithGroups;
        _groupRecycler.getAdapter().notifyDataSetChanged();

        Intent groupResultIntent = new Intent();
        setResult(RESULT_OK, groupResultIntent);

        finish();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.txt_btn_end) {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();

        NethPermissionWithOpGroups.saveGroupUser(
                this,
                new Gson().toJson(userWithGroupsSelected),
                NethPermissionWithOpGroups.getGroupsUserFile(this)
        );

        overridePendingTransition(
                R.anim.slide_in_top_to_bottom,
                R.anim.slide_out_top_to_bottom
        );
    }

    private void setUiStatusOk() {
        terminateAllProgress();
        hideAlertMessage();
    }

    private void setUiStatusFailureWithMessage(String message) {
        terminateAllProgress();
        showAlertMessage(message);
    }

    private void showAlertMessage(String message) {
        terminateAllProgress();
        _groupRecycler.setVisibility(GONE);
        _txtAlertGroups.setText(message);
        _txtAlertGroups.setVisibility(VISIBLE);
    }

    private void hideAlertMessage() {
        _groupRecycler.setVisibility(VISIBLE);
        _txtAlertGroups.setVisibility(GONE);
    }

    private void terminateAllProgress() {
        swpGroups.setRefreshing(false);
        progressBar.setVisibility(GONE);
    }

}
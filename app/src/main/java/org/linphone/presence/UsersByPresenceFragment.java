package org.linphone.presence;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_BOOK;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_CALL;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_CLOSE_POPUP;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_HANGUP;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_INTRUDE;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_PICKUP;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_RECORD;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_SPY;
import static org.linphone.presence.PresenceStatusActivity.STATUS_SELECTED;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.NethCTIApplication;
import org.linphone.R;
import org.linphone.core.ProxyConfig;
import org.linphone.interfaces.OnActionResul;
import org.linphone.interfaces.OnAdapterItemListener;
import org.linphone.interfaces.OnUserActionListener;
import org.linphone.settings.LinphonePreferences;
import org.linphone.utils.LinphoneUtils;
import org.linphone.utils.SharedPreferencesManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import it.nethesis.models.NethPermissionWithOpGroups;
import it.nethesis.models.NethUser;
import it.nethesis.models.OpGroupsUsers;
import it.nethesis.models.decorator.MainPresenceDecorator;
import it.nethesis.models.decorator.PresenceDecorator;
import it.nethesis.models.presence.PresenceUser;
import it.nethesis.webservices.ActionCallRestAPI;
import it.nethesis.utils.RefreshAction;
import it.nethesis.utils.TimerSingleton;
import it.nethesis.webservices.PresenceRestAPI;
import it.nethesis.webservices.RetrofitGenerator;
import it.nethesis.webservices.UserRestAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UsersByPresenceFragment extends Fragment implements
        Callback<HashMap<String, PresenceUser>>, View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, OnAdapterItemListener<PresenceUser>,
        OnUserActionListener, RefreshAction, OnActionResul, View.OnScrollChangeListener {

    private static final int GROUPS_BUTTON = 0;
    private static final int FAVORITES_BUTTON = 1;

    private ProxyConfig mProxyConfig;
    private int mAccountIndex = 0;

    private UserRestAPI _userRestAPI;
    private PresenceRestAPI _presenceRestAPI;
    private ActionCallRestAPI _actionCallRestAPI;

    private PresenceActionsBottomDialog bottomSheetActionsDialog;

    private SwipeRefreshLayout swipeRefreshListPresence;
    private RecyclerView recyclerContactsPresence;
    private ProgressBar progressBar;
    private MaterialButton btnGroup;
    private MaterialButton btnFavorites;
    private TextView txtAlertMessage;
    private ImageView btnBack;

    private ConstraintLayout constraintStatus;
    private ImageView imgStatus;
    private TextView txtStatus;

    private NethUser _nethUser;
    private HashMap<String, OpGroupsUsers> _presenceGroups = new HashMap<>();
    private ArrayList<PresenceUser> userList = new ArrayList();

    private NethPermissionWithOpGroups nethSelectedWithOpGroups;
    private PresenceActionManager presenceActionManager;

    private boolean firstCreation = false;
    private Parcelable recyclerViewState;

    public static UsersByPresenceFragment newInstance() {
        UsersByPresenceFragment fragment = new UsersByPresenceFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String domain = SharedPreferencesManager.getDomain(getContext());
        _userRestAPI = RetrofitGenerator.createService(UserRestAPI.class, domain);
        _presenceRestAPI = RetrofitGenerator.createService(PresenceRestAPI.class, domain);
        _actionCallRestAPI = RetrofitGenerator.createService(ActionCallRestAPI.class, domain);
        presenceActionManager = new PresenceActionManager(
                getContext(),
                this,
                _actionCallRestAPI
        );
        nethSelectedWithOpGroups = NethPermissionWithOpGroups.restoreGroupUser(
                getContext(),
                NethPermissionWithOpGroups.getGroupsUserFile(getContext())
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_users_by_presence,
                container,
                false
        );

        // Get the current user proxy config, useful for logout by now.
        mProxyConfig = LinphoneManager.getUserProxyConfig(mAccountIndex);

        constraintStatus = view.findViewById(R.id.cnstrnt_status);
        imgStatus = view.findViewById(R.id.img_status);
        txtStatus = view.findViewById(R.id.txt_status);
        recyclerContactsPresence = view.findViewById(R.id.rclr_contacts_presence);
        progressBar = view.findViewById(R.id.progress_bar);
        btnGroup = view.findViewById(R.id.btn_group);
        btnFavorites = view.findViewById(R.id.btn_favorites);
        txtAlertMessage = view.findViewById(R.id.txt_alert_message);
        swipeRefreshListPresence = view.findViewById(R.id.SwipeRefresh_presence_list);
        btnBack = view.findViewById(R.id.cancel);

        if(Build.VERSION.SDK_INT < 26){
            txtStatus.setPadding(0, 0, 10, 0);
            txtStatus.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            txtStatus.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switchToBetweenGroupOrFavoritesButtons(GROUPS_BUTTON);

        setDayNightThemeColor();
        btnGroup.setText(R.string.presence_group);
        btnGroup.setOnClickListener(this);
        btnFavorites.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        swipeRefreshListPresence.setOnRefreshListener(this);
        constraintStatus.setOnClickListener(this);

        firstCreation = true;
        TimerSingleton.initialize(this);
    }

    private void setDayNightThemeColor() {
        boolean darkMode = NethCTIApplication
                .Companion
                .getDayNightThemeColor();

        int color = darkMode
                ? R.color.ic_presence_color_gray_text_selector_dark
                : R.color.ic_presence_color_gray_text_selector;
        btnFavorites.setTextColor(getContext().getColorStateList(color));
        btnGroup.setTextColor(getContext().getColorStateList(color));
    }

    @Override
    public void onStart() {
        super.onStart();
        requireUserList(false);
        TimerSingleton.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        TimerSingleton.stop();
    }

    @Override
    public void refreshContactList() {
        if (!isAdded()) return;
        requireUserList(true);
    }

    private void requireUserList(boolean pullToRefresh) {
        _presenceRestAPI.getAllPresenceUsers(
                SharedPreferencesManager.getAuthtoken(getContext())
        ).enqueue(UsersByPresenceFragment.this);
        if (pullToRefresh) return;
        if (firstCreation){
            progressBar.setVisibility(VISIBLE);
            firstCreation = false;
        }
    }

    @Override
    public void onRefresh() {
        requireUserList(true);
    }

    // scarico la lista di tutti gli utenti disponibili
    @Override
    public void onResponse(
            Call<HashMap<String, PresenceUser>> call,
            Response<HashMap<String, PresenceUser>> response
    ) {
        if (!isAdded()) return;
        progressBar.setVisibility(GONE);

        switch (response.code()) {
            case 200:
                userList.clear();
                if (response.body() != null)
                    userList = new ArrayList(response.body().values());

                _userRestAPI.getMe(SharedPreferencesManager.getAuthtoken(getContext()))
                        .enqueue(nethUser);

                if (!swipeRefreshListPresence.isRefreshing())
                    progressBar.setVisibility(VISIBLE);

                hideAlertMessage();
                break;
            case 401:
                setOfflineStatus();
                showAlertMessage(getString(R.string.neth_session_expired));
                break;
            default:
                showAlertMessage(getString(R.string.neth_something_wrong));
                break;
        }
    }

    @Override
    public void onFailure(Call<HashMap<String, PresenceUser>> call, Throwable t) {
        if (!isAdded()) return;
        showAlertMessage(getString(R.string.neth_no_connection));
        setOfflineStatus();
    }

    // scarico i permessi singolo utente (utente loggato)
    private Callback<NethUser> nethUser = new Callback<NethUser>() {
        @Override
        public void onResponse(Call<NethUser> call, Response<NethUser> response) {
            if (!isAdded()) return;
            progressBar.setVisibility(GONE);

            switch (response.code()) {
                case 200:
                    _nethUser = response.body();
                    if (_nethUser == null ) {
                        showAlertMessage(getString(R.string.neth_something_wrong));
                        setOfflineStatus();
                        return;
                    }

                    setMePresenceStatus();

                    _presenceRestAPI.getAllPresenceGroups(
                            SharedPreferencesManager.getAuthtoken(getContext())
                    ).enqueue(groupResponse);

                    if (!swipeRefreshListPresence.isRefreshing())
                        progressBar.setVisibility(VISIBLE);

                    hideAlertMessage();
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
        public void onFailure(Call<NethUser> call, Throwable t) {
            if (!isAdded()) return;
            showAlertMessage(getString(R.string.neth_no_connection));
        }
    };

    // scarico la lista dei gruppi
    private final Callback<HashMap<String, OpGroupsUsers>> groupResponse =
            new Callback<HashMap<String, OpGroupsUsers>>() {
                @Override
                public void onResponse(
                        Call<HashMap<String, OpGroupsUsers>> call,
                        Response<HashMap<String, OpGroupsUsers>> response
                ) {
                    if (!isAdded()) return;
                    switch (response.code()) {
                        case 200:
                            hideAlertMessage();

                            _presenceGroups = response.body();
                            saveAndAssingNethGroupWithUsers();
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
                    if (!isAdded()) return;
                    showAlertMessage(getString(R.string.neth_no_connection));
                }
            };

    private void setMePresenceStatus() {
        if (_nethUser == null) return;

        boolean disabled = !mProxyConfig.registerEnabled();
        PresenceDecorator decorator = new PresenceDecorator(_nethUser.getPresence(disabled));

        Drawable backgroundPresenceSquare = ContextCompat
                .getDrawable(getContext(), decorator.getStatusSquareSelectorBackground());
        constraintStatus.setBackground(backgroundPresenceSquare);
        imgStatus.setImageResource(decorator.getBackgroundCircleStatusMeIcon());
        txtStatus.setText(decorator.getStatusLabel());
    }

    private void terminateAllProgress() {
        if (swipeRefreshListPresence.isRefreshing())
            swipeRefreshListPresence.setRefreshing(false);
        progressBar.setVisibility(GONE);
    }

    private void showAlertMessage(String message) {
        terminateAllProgress();
        recyclerContactsPresence.setVisibility(GONE);
        txtAlertMessage.setText(message);
        txtAlertMessage.setVisibility(VISIBLE);
    }

    private void hideAlertMessage() {
        txtAlertMessage.setText("");
        txtAlertMessage.setVisibility(GONE);
    }

    private void saveAndAssingNethGroupWithUsers() {
        ArrayList<NethPermissionWithOpGroups> assigned = NethPermissionWithOpGroups.getNethsWithAssignedGroup(
                _nethUser.getNethPermissions(),
                _presenceGroups
        );

        NethPermissionWithOpGroups.sortListOfNethPermissionWithOpGroups(assigned);

        if (!assigned.isEmpty()) {
            //lo assegno solo se non è stato già selezionato
            if (nethSelectedWithOpGroups == null) {
                Log.e("NethPermissionWithOpGroups", "FIRST TIME SAVING");
                nethSelectedWithOpGroups = assigned.get(0);
                NethPermissionWithOpGroups.saveGroupUser(
                        getContext(),
                        new Gson().toJson(nethSelectedWithOpGroups),
                        NethPermissionWithOpGroups.getGroupsUserFile(getContext())
                );
            } else {
                //aggiorna nethSelectedWithOpGroups con i dati ricevuti dal server
                Log.e("NethPermissionWithOpGroups", "UPDATE");
                for (NethPermissionWithOpGroups item : assigned) {
                    if (item.nethPermission.id.equals(nethSelectedWithOpGroups.nethPermission.id)) {
                        nethSelectedWithOpGroups = item;
                        NethPermissionWithOpGroups.saveGroupUser(
                                getContext(),
                                new Gson().toJson(nethSelectedWithOpGroups),
                                NethPermissionWithOpGroups.getGroupsUserFile(getContext())
                        );
                        break;
                    }
                }
            }
        }

        showSelectedGroup();
        showPresenceListBySectionSelected();

    }

    private void showPresenceListBySectionSelected() {
        if (btnGroup.isSelected())
            showPresenceListFilteredByGroups();
         else if (btnFavorites.isSelected())
            showPresenceListFilteredByFavorites();
        else
            showPresenceListFilteredByGroups();
    }

    private void showSelectedGroup() {
        String groupName = null;
        if (nethSelectedWithOpGroups == null) return;
        if (!nethSelectedWithOpGroups.opGroupUsers.keySet().isEmpty())
            groupName = nethSelectedWithOpGroups
                    .opGroupUsers
                    .keySet()
                    .toArray()[0]
                    .toString();
        String groupNameToShow = groupName == null
                ? getString(R.string.presence_group)
                : groupName;
        btnGroup.setText(groupNameToShow);

        //switchToBetweenGroupOrFavoritesButtons(GROUPS_BUTTON);

    }

    private void showPresenceListFilteredByGroups() {
        ArrayList<PresenceUser> groupUsers = matchOpGroupWithContactList();
        if (groupUsers.isEmpty()) {
            showAlertMessage(getString(R.string.neth_no_contacts));
        } else hideAlertMessage();

        showContactList(groupUsers);
    }

    private ArrayList<PresenceUser> matchOpGroupWithContactList() {
        ArrayList<PresenceUser> groupUsers = new ArrayList();
        if (nethSelectedWithOpGroups == null) return groupUsers;
        if (nethSelectedWithOpGroups.opGroupUsers == null || nethSelectedWithOpGroups.opGroupUsers.isEmpty())
            return groupUsers;

        HashMap<String, OpGroupsUsers> opGroupUsers = nethSelectedWithOpGroups.opGroupUsers;

        if (opGroupUsers.keySet().isEmpty()) return groupUsers;
        String key = opGroupUsers.keySet().toArray()[0].toString();

        if (opGroupUsers.get(key) == null || opGroupUsers.get(key) == null) return groupUsers;
        ArrayList<String> users = opGroupUsers.get(key).users;

        for (String user : users) {
            for (PresenceUser presenceUser: userList) {
                if (presenceUser.username.equals(user)) {
                    groupUsers.add(presenceUser);
                    break;
                }
            }
        }

        return groupUsers;
    }

    private void showPresenceListFilteredByFavorites() {
        ArrayList<PresenceUser> favorites = PresenceUser.getFavoritesContacts(
                getContext(),
                userList
        );
        if (favorites.isEmpty())
            showAlertMessage(getString(R.string.neth_no_contacts));
        else hideAlertMessage();

        showContactList(PresenceUser.getFavoritesContacts(getContext(), userList));
    }

    private void showContactList(ArrayList<PresenceUser> contacts) {
        Collections.sort(contacts, (user1, user2) -> {
            String keyToString1 = user1.name;
            String keyToString2 = user2.name;
            return keyToString1.compareToIgnoreCase(keyToString2);
        });
        terminateAllProgress();
        recyclerContactsPresence.setVisibility(VISIBLE);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        ContactPresenceListAdapter adapter = new ContactPresenceListAdapter(
                mProxyConfig,
                contacts,
                this
        );

        recyclerContactsPresence.setLayoutManager(layoutManager);
        recyclerContactsPresence.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // Restore state
        recyclerContactsPresence.getLayoutManager().onRestoreInstanceState(recyclerViewState);

        recyclerContactsPresence.setOnScrollChangeListener(this);

    }

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        // Save state
        recyclerViewState = recyclerContactsPresence
                .getLayoutManager()
                .onSaveInstanceState();
    }

    @Override
    public void onResume() {
        super.onResume();
        constraintStatus.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_group) {
            if (!v.isSelected()) {
                switchToBetweenGroupOrFavoritesButtons(GROUPS_BUTTON);
                showPresenceListFilteredByGroups();
                return;
            }
            showGroupSheetDialog();
        }
        else if (v.getId() == R.id.btn_favorites) {
            switchToBetweenGroupOrFavoritesButtons(FAVORITES_BUTTON);
            showPresenceListFilteredByFavorites();
        } else if (v.getId() == R.id.cnstrnt_status) {
            constraintStatus.setEnabled(false);
            Intent gotoStatusIntent = new Intent(getContext(), PresenceStatusActivity.class);
            boolean disabled = !mProxyConfig.registerEnabled();
            gotoStatusIntent.putExtra(STATUS_SELECTED, _nethUser == null ? null :_nethUser.getPresence(disabled));
            startActivityForResult(gotoStatusIntent, PRESENCE_STATUS_LIST_REQUEST);
        } else if (v.getId() == R.id.cancel) {
            LinphoneActivity parent = LinphoneActivity.instance();

            if(!parent.isTablet()){
                getActivity().getFragmentManager().popBackStack();
            } else {
                if (LinphoneManager.getLc().getProxyConfigList().length == 0) {
                    LinphoneActivity.instance().finish();
                } else {
                    parent.hideTopBar();
                    parent.displayDashboard();
                }
            }
        }
    }
    
    public void showGroupSheetDialog() {
        Intent showGroupsIntent = new Intent(getContext(), PresenceGroupsDialogActivity.class);
        //if (_presenceGroups == null || _presenceGroups.isEmpty()) return;
        //TODO mettere messaggio di errore

        startActivityForResult(showGroupsIntent, PRESENCE_GROUP_DIALOG_REQUEST);
        getActivity().overridePendingTransition(
                R.anim.slide_in_bottom_to_top,
                R.anim.slide_out_bottom_to_top
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ritorno dalla selezione del gruppo
        if (requestCode == PRESENCE_GROUP_DIALOG_REQUEST) {
            switch (resultCode) {
                case RESULT_OK:
                    nethSelectedWithOpGroups = NethPermissionWithOpGroups
                            .restoreGroupUser(
                                    getContext(),
                                    NethPermissionWithOpGroups.getGroupsUserFile(getContext())
                            );

                    showSelectedGroup();
                    showPresenceListFilteredByGroups();
                    break;
                case RESULT_CANCELED:
                    showSelectedGroup();
                    showPresenceListFilteredByGroups();
                    break;
            }
        } else if (requestCode == PRESENCE_STATUS_LIST_REQUEST) {
            switch (resultCode) {
                case RESULT_OK:
                    requireUserList(false);
                    break;
                case RESULT_CANCELED: break;
            }
        }
    }

    private void switchToBetweenGroupOrFavoritesButtons(int to) {
        if (to == GROUPS_BUTTON) {
            setGoupButtonSelected(true);
            setFavoritesButtonSelected(false);
        } else {
            setFavoritesButtonSelected(true);
            setGoupButtonSelected(false);
        }
    }

    private void setGoupButtonSelected(boolean selected) {
        btnGroup.setSelected(selected);
        Drawable top = ContextCompat.getDrawable(
                getContext(),
                selected
                        ? R.drawable.ic_presence_circle_group_blue_selector
                        : R.drawable.ic_presence_circle_group_gray_selector
        );
        btnGroup.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
        /*btnGroup.setTextColor(
                selected
                        ? R.drawable.ic_presence_color_blue_text_selector
                        : R.drawable.ic_presence_color_gray_text_selector
        );*/
    }

    private void setFavoritesButtonSelected(boolean selected) {
        btnFavorites.setSelected(selected);
        Drawable top = ContextCompat.getDrawable(
                getContext(),
                selected
                        ? R.drawable.ic_presence_circle_favorites_blue_selector
                        : R.drawable.ic_presence_circle_favorites_gray_selector
        );
        btnFavorites.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
        /*btnFavorites.setTextColor(
                selected
                        ? R.drawable.ic_presence_color_blue_text_selector
                        : R.drawable.ic_presence_color_gray_text_selector
        );*/
    }

    @Override
    public void onClickItem(PresenceUser presenceUser) {
        bottomSheetActionsDialog = new PresenceActionsBottomDialog(
                getContext(),
                presenceUser,
                _nethUser,
                this
        );
        bottomSheetActionsDialog.show();
    }

    @Override
    public void onFavoritePressed(boolean isFavorite, PresenceUser presenceUser) {
        if (isFavorite)
            PresenceUser.putFavorite(getContext(), presenceUser.username);
        else
            PresenceUser.removeFavorite(getContext(), presenceUser.username);
        if (btnFavorites.isSelected())
            showPresenceListFilteredByFavorites();

    }

    @Override
    public void onActionButtonPressed(int action, PresenceUser target) {
        if (bottomSheetActionsDialog != null) {
            if (bottomSheetActionsDialog.isShowing())
                bottomSheetActionsDialog.dismiss();
        }

        switch (action) {
            case ACTION_CLOSE_POPUP: return;
            case ACTION_CALL:
                callTargetUser(target);
                break;
            case ACTION_HANGUP:
                presenceActionManager.sendHangupRequest(_nethUser, target);
                progressBar.setVisibility(VISIBLE);
                break;
            case ACTION_RECORD:
                presenceActionManager.sendStartRecordRequest(target);
                progressBar.setVisibility(VISIBLE);
                break;
            case ACTION_INTRUDE:
                presenceActionManager.sendIntrudeRequest(_nethUser, target);
                progressBar.setVisibility(VISIBLE);
                break;
            case ACTION_BOOK:
                presenceActionManager.sendBookRequest(_nethUser, target);
                progressBar.setVisibility(VISIBLE);
                break;
            case ACTION_PICKUP:
                presenceActionManager.sendPickupRequest(_nethUser, target);
                progressBar.setVisibility(VISIBLE);
                break;
            case ACTION_SPY:
                presenceActionManager.sendSpyRequest(_nethUser, target);
                progressBar.setVisibility(VISIBLE);
                break;
        }
        requireUserList(false);
    }

    private void callTargetUser(PresenceUser target) {
        String mainextensionId = target.endpoints.getMainExtensionId();
        String fullAddress = LinphoneUtils.getFullAddressFromUsername(mainextensionId);
        LinphoneActivity.instance().setAddresGoToDialerAndCall(fullAddress, target.name);
    }

    @Override
    public void onSuccess() {
        progressBar.setVisibility(GONE);
    }

    @Override
    public void onFailure(int actionType) {
        progressBar.setVisibility(GONE);
        String alertMessage = "";
        switch (actionType) {
            case ACTION_HANGUP:
                alertMessage = String.format(
                        getString(R.string.presence_action_impossible),
                        getString(R.string.presence_action_hangup)
                );
                break;
            case ACTION_RECORD:
                alertMessage = String.format(
                        getString(R.string.presence_action_impossible),
                        getString(R.string.presence_action_record)
                );
                break;
            case ACTION_INTRUDE:
                alertMessage = String.format(
                        getString(R.string.presence_action_impossible),
                        getString(R.string.presence_action_intrude)
                );
                break;
            case ACTION_BOOK:
                alertMessage = String.format(
                        getString(R.string.presence_action_impossible),
                        getString(R.string.presence_action_book)
                );
                break;
            case ACTION_PICKUP:
                alertMessage = String.format(
                        getString(R.string.presence_action_impossible),
                        getString(R.string.presence_action_pickup)
                );
                break;
            case ACTION_SPY:
                alertMessage = String.format(
                        getString(R.string.presence_action_impossible),
                        getString(R.string.presence_action_spy)
                );
                break;
        }

        new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.presence_attention_title))
                .setMessage(alertMessage)
                .setCancelable(false)
                .setNeutralButton(R.string.presence_alert_ok, (dialogInterface, i) ->
                        dialogInterface.dismiss()
                )
                .create()
                .show();
    }

    private void setOfflineStatus(){
        MainPresenceDecorator decorator = new MainPresenceDecorator(MainPresenceDecorator.MAIN_PRESENCE_STATUS_N_D);
        Drawable backgroundPresenceSquare = ContextCompat
                .getDrawable(getContext(), decorator.getStatusSquareSelectorBackground());

        constraintStatus.setBackground(backgroundPresenceSquare);
        imgStatus.setImageResource(decorator.getBackgroundCircleStatusMeIcon());
        txtStatus.setText(decorator.getStatusLabel());
    }

    public static final int PRESENCE_GROUP_DIALOG_REQUEST = 1000;
    public static final int PRESENCE_STATUS_LIST_REQUEST = 1001;

}
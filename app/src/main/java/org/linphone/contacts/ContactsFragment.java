package org.linphone.contacts;

/*
ContactsFragment.java
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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import it.nethesis.models.NethesisContact;
import it.nethesis.models.contactlist.Contact;
import it.nethesis.models.contactlist.ContactList;
import it.nethesis.webservices.RetrofitGenerator;
import it.nethesis.webservices.UserRestAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.assistant.AssistantActivity;
import org.linphone.fragments.FragmentsAvailable;
import org.linphone.utils.SelectableHelper;
import org.linphone.utils.SharedPreferencesManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsFragment extends Fragment
        implements OnItemClickListener,
                ContactsUpdatedListener,
                ContactViewHolder.ClickListener,
                SelectableHelper.DeleteListener {
    private RecyclerView mContactsList;
    private TextView mNoSipContact, mNoContact;
    private ImageView mAllContacts, mLinphoneContacts, mNewContact, mEdit;
    private boolean mOnlyDisplayLinphoneContacts;
    private View mAllContactsSelected, mLinphoneContactsSelected;
    private int mLastKnownPosition;
    private boolean mEditOnClick = false, mEditConsumed = false, mOnlyDisplayChatAddress = false;
    private String mSipAddressToAdd, mDisplayName = null;
    private SearchView mSearchView;
    private ProgressBar mContactsFetchInProgress;
    private LinearLayoutManager mLayoutManager;
    private Context mContext;
    private SelectableHelper mSelectionHelper;
    private ContactsAdapter mContactAdapter;
    private SwipeRefreshLayout mContactsRefresher;
    private List<LinphoneContact> listContact;
    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private int currentPage = PAGE_START;
    private static final int LIMIT = 50;
    private static final String ALL = "all";
    private String mView = ALL;
    private RelativeLayout mRelativeLayoutViews;
    private Spinner mSpinnerView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts_list, container, false);
        mContext = getActivity().getApplicationContext();
        mSelectionHelper = new SelectableHelper(view, this);
        mSelectionHelper.setDialogMessage(R.string.delete_contacts_text);

        if (getArguments() != null) {
            mEditOnClick = getArguments().getBoolean("EditOnClick");
            mSipAddressToAdd = getArguments().getString("SipAddress");
            if (getArguments().getString("DisplayName") != null) {
                mDisplayName = getArguments().getString("DisplayName");
            }
            mOnlyDisplayChatAddress = getArguments().getBoolean("ChatAddressOnly");

            if (getArguments().getBoolean("EditOnClick")) {
                Toast.makeText(
                                LinphoneActivity.instance(),
                                R.string.toast_choose_contact_for_edition,
                                Toast.LENGTH_LONG)
                        .show();
            }
            getArguments().clear();
        }

        mNoSipContact = view.findViewById(R.id.noSipContact);
        mNoContact = view.findViewById(R.id.noContact);
        mContactsList = view.findViewById(R.id.contactsList);

        mAllContacts = view.findViewById(R.id.all_contacts);
        mLinphoneContacts = view.findViewById(R.id.linphone_contacts);
        mAllContactsSelected = view.findViewById(R.id.all_contacts_select);
        mLinphoneContactsSelected = view.findViewById(R.id.linphone_contacts_select);
        mContactsFetchInProgress = view.findViewById(R.id.contactsFetchInProgress);
        mNewContact = view.findViewById(R.id.newContact);
        mEdit = view.findViewById(R.id.edit);
        mContactsRefresher = view.findViewById(R.id.contactsListRefresher);

        mRelativeLayoutViews = view.findViewById(R.id.relativeLayout_views);
        mSpinnerView = view.findViewById(R.id.spinner_views);

        ArrayAdapter mViewAdapater =
                ArrayAdapter.createFromResource(
                        getActivity().getApplicationContext(),
                        R.array.views,
                        android.R.layout.simple_spinner_dropdown_item);
        mViewAdapater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerView.setAdapter(mViewAdapater);

        mContactsRefresher.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (!mOnlyDisplayLinphoneContacts) {
                            ContactsManager.getInstance().fetchContactsAsync();
                        } else {
                            searchContactsNethesis(
                                    mView,
                                    ContactsFragment.this,
                                    mSearchView.getQuery().toString(),
                                    LIMIT * currentPage,
                                    true,
                                    true,
                                    false);
                        }
                    }
                });

        mAllContacts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnlyDisplayLinphoneContacts = false;
                        mAllContactsSelected.setVisibility(View.VISIBLE);
                        mAllContacts.setEnabled(false);
                        mLinphoneContacts.setEnabled(true);
                        mLinphoneContactsSelected.setVisibility(View.INVISIBLE);
                        mNewContact.setVisibility(View.VISIBLE);
                        mNewContact.setEnabled(true);
                        mEdit.setVisibility(View.VISIBLE);
                        mEdit.setEnabled(true);

                        changeContactsAdapter();
                    }
                });

        mLinphoneContacts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAllContactsSelected.setVisibility(View.INVISIBLE);
                        mLinphoneContactsSelected.setVisibility(View.VISIBLE);
                        mLinphoneContacts.setEnabled(false);
                        mAllContacts.setEnabled(true);
                        mOnlyDisplayLinphoneContacts = true;
                        mNewContact.setVisibility(View.GONE);
                        mNewContact.setEnabled(false);
                        mEdit.setVisibility(View.GONE);
                        mEdit.setEnabled(false);
                        changeContactsAdapter();
                    }
                });

        mNewContact.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEditConsumed = true;
                        ContactsManager.getInstance()
                                .createContact(getActivity(), mDisplayName, mSipAddressToAdd);
                    }
                });

        if (getResources().getBoolean(R.bool.hide_non_linphone_contacts)) {
            mAllContacts.setEnabled(false);
            mLinphoneContacts.setEnabled(false);
            mOnlyDisplayLinphoneContacts = true;
            mAllContacts.setOnClickListener(null);
            mLinphoneContacts.setOnClickListener(null);
            mLinphoneContacts.setVisibility(View.INVISIBLE);
            mLinphoneContactsSelected.setVisibility(View.INVISIBLE);
        } else {
            mAllContacts.setEnabled(mOnlyDisplayLinphoneContacts);
            mLinphoneContacts.setEnabled(!mAllContacts.isEnabled());
        }
        mNewContact.setEnabled(LinphoneManager.getLc().getCallsNb() == 0);

        if (!ContactsManager.getInstance().contactsFetchedOnce()) {
            if (ContactsManager.getInstance().hasReadContactsAccess()) {
                mContactsFetchInProgress.setVisibility(View.VISIBLE);
            } else {
                LinphoneActivity.instance().checkAndRequestReadContactsPermission();
            }
        } else {
            if (!mOnlyDisplayLinphoneContacts
                    && ContactsManager.getInstance().getContacts().size() == 0) {
                mNoContact.setVisibility(View.VISIBLE);
            } else if (mOnlyDisplayLinphoneContacts
                    && ContactsManager.getInstance().getSIPContacts().size() == 0) {
                mNoSipContact.setVisibility(View.VISIBLE);
            }
        }

        mSearchView = view.findViewById(R.id.searchField);
        mSearchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (mSearchView.hasFocus()) {
                            currentPage = 0;
                            searchContacts(newText);
                        }

                        return true;
                    }
                });

        mLayoutManager = new LinearLayoutManager(mContext);
        mContactsList.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(
                        mContactsList.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(
                getActivity().getResources().getDrawable(R.drawable.divider));
        mContactsList.addItemDecoration(dividerItemDecoration);

        mContactsList.addOnScrollListener(
                new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(
                            @NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            isLoading = true;
                        }
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int rows = ContactsManager.getInstance().getSIPContacts().size();
                        int total = ContactsManager.getInstance().getTotalNethesisContactCount();

                        int visibleItemCount = mLayoutManager.getChildCount();
                        int firstVisibleItemPosition =
                                mLayoutManager.findFirstVisibleItemPosition();
                        if (isLoading()) {
                            if (rows < total
                                    && ((visibleItemCount + firstVisibleItemPosition) + 20)
                                            > rows) {
                                loadMoreContacts(
                                        mView,
                                        mSearchView.getQuery().toString(),
                                        mSearchView.getQuery() != null
                                                && mSearchView.getQuery().length() != 0);
                            }
                        }
                    }
                });

        mSpinnerView.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> adapterView, View view, int position, long l) {

                        setSelectView(adapterView.getItemAtPosition(position).toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {}
                });

        return view;
    }

    public void displayFirstContact() {
        if (mContactsList != null
                && mContactsList.getAdapter() != null
                && mContactsList.getAdapter().getItemCount() > 0) {
            ContactsAdapter mAdapt = (ContactsAdapter) mContactsList.getAdapter();
            LinphoneActivity.instance().displayContact((LinphoneContact) mAdapt.getItem(0), false);
        } else {
            LinphoneActivity.instance().displayEmptyFragment();
        }
    }

    private void searchContacts(String search) {
        boolean isEditionEnabled = false;
        if (search == null || search.length() == 0) {
            changeContactsAdapter();
            return;
        }
        changeContactsToggle();

        if (mOnlyDisplayLinphoneContacts) {
            searchContactsNethesis(mView, this, search, LIMIT * currentPage, true, false, true);
        } else {
            listContact = ContactsManager.getInstance().getContacts(search);
        }

        if (mContactAdapter != null && mContactAdapter.isEditionEnabled()) {
            isEditionEnabled = true;
        }

        mContactAdapter = new ContactsAdapter(mContext, listContact, this, mSelectionHelper);
        mContactAdapter.setIsSearchMode(true);

        //		mContactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mSelectionHelper.setAdapter(mContactAdapter);
        if (isEditionEnabled) {
            mSelectionHelper.enterEditionMode();
        }
        mContactsList.setAdapter(mContactAdapter);
    }

    private void changeContactsAdapter() {
        changeContactsToggle();
        // List<LinphoneContact> listContact;
        mSearchView.clearFocus();
        mSearchView.setQuery("", false);
        mNoSipContact.setVisibility(View.GONE);
        mNoContact.setVisibility(View.GONE);
        mContactsList.setVisibility(View.VISIBLE);
        boolean isEditionEnabled = false;
        String query = mSearchView.getQuery().toString();
        if (query.equals("")) {
            if (mOnlyDisplayLinphoneContacts) {
                searchContactsNethesis(mView, this, "", LIMIT * currentPage, false, false, true);
            } else {
                listContact = ContactsManager.getInstance().getContacts();
            }
        } else {
            if (mOnlyDisplayLinphoneContacts) {
                searchContactsNethesis(mView, this, query, LIMIT * currentPage, false, false, true);
            } else {
                listContact = ContactsManager.getInstance().getContacts(query);
            }
        }

        if (mContactAdapter != null && mContactAdapter.isEditionEnabled()) {
            isEditionEnabled = true;
        }

        mContactAdapter = new ContactsAdapter(mContext, listContact, this, mSelectionHelper);

        mSelectionHelper.setAdapter(mContactAdapter);

        if (isEditionEnabled) {
            mSelectionHelper.enterEditionMode();
        }
        mContactsList.setAdapter(mContactAdapter);

        mContactAdapter.notifyDataSetChanged();

        if (!mOnlyDisplayLinphoneContacts && mContactAdapter.getItemCount() == 0) {
            mNoContact.setVisibility(View.VISIBLE);
        } else if (mOnlyDisplayLinphoneContacts && mContactAdapter.getItemCount() == 0) {
            mNoSipContact.setVisibility(View.VISIBLE);
        }
    }

    private void changeContactsToggle() {
        if (mOnlyDisplayLinphoneContacts
                && !getResources().getBoolean(R.bool.hide_non_linphone_contacts)) {
            mAllContacts.setEnabled(true);
            mAllContactsSelected.setVisibility(View.INVISIBLE);
            mLinphoneContacts.setEnabled(false);
            mLinphoneContactsSelected.setVisibility(View.VISIBLE);
            mRelativeLayoutViews.setVisibility(View.VISIBLE);
        } else {
            mAllContacts.setEnabled(false);
            mAllContactsSelected.setVisibility(View.VISIBLE);
            mLinphoneContacts.setEnabled(true);
            mLinphoneContactsSelected.setVisibility(View.INVISIBLE);
            mRelativeLayoutViews.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        LinphoneContact contact = (LinphoneContact) adapter.getItemAtPosition(position);
        if (mEditOnClick) {
            mEditConsumed = true;
            ContactsManager.getInstance().editContact(getActivity(), contact, mSipAddressToAdd);
        } else {
            mLastKnownPosition = mLayoutManager.findFirstVisibleItemPosition();
            LinphoneActivity.instance().displayContact(contact, mOnlyDisplayChatAddress);
        }
    }

    @Override
    public void onItemClicked(int position) {
        LinphoneContact contact = (LinphoneContact) mContactAdapter.getItem(position);

        if (mContactAdapter.isEditionEnabled()) {
            mContactAdapter.toggleSelection(position);

        } else if (mEditOnClick) {
            mEditConsumed = true;
            ContactsManager.getInstance().editContact(getActivity(), contact, mSipAddressToAdd);
        } else {
            mLastKnownPosition = mLayoutManager.findFirstVisibleItemPosition();
            LinphoneActivity.instance().displayContact(contact, mOnlyDisplayChatAddress);
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!mContactAdapter.isEditionEnabled()) {
            mSelectionHelper.enterEditionMode();
        }
        mContactAdapter.toggleSelection(position);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ContactsManager.getInstance().addContactsListener(this);

        if (mEditConsumed) {
            mEditOnClick = false;
            mSipAddressToAdd = null;
        }

        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().selectMenu(FragmentsAvailable.CONTACTS_LIST);
            mOnlyDisplayLinphoneContacts =
                    ContactsManager.getInstance().isLinphoneContactsPrefered()
                            || getResources().getBoolean(R.bool.hide_non_linphone_contacts);
        }
        changeContactsToggle();
        invalidate();
    }

    @Override
    public void onPause() {
        ContactsManager.getInstance().removeContactsListener(this);
        super.onPause();
    }

    @Override
    public void onContactsUpdated() {
        if (!LinphoneActivity.isInstanciated()
                || (LinphoneActivity.instance().getCurrentFragment()
                                != FragmentsAvailable.CONTACTS_LIST
                        && !LinphoneActivity.instance().isTablet())) return;
        if (mContactAdapter != null) {
            mContactAdapter.updateDataSet(
                    mOnlyDisplayLinphoneContacts
                            ? ContactsManager.getInstance().getSIPContacts()
                            : ContactsManager.getInstance().getContacts());
            mContactAdapter.notifyDataSetChanged();

            if (mContactAdapter.getItemCount() > 0) {
                mNoContact.setVisibility(View.GONE);
                mNoSipContact.setVisibility(View.GONE);
            }
        }
        mContactsFetchInProgress.setVisibility(View.GONE);
        mContactsRefresher.setRefreshing(false);
    }

    private void invalidate() {
        if (mSearchView != null && mSearchView.getQuery().toString().length() > 0) {
            searchContacts(mSearchView.getQuery().toString());
        } else {
            changeContactsAdapter();
        }
        mContactsList.scrollToPosition(mLastKnownPosition);
    }

    @Override
    public void onDeleteSelection(Object[] objectsToDelete) {
        ArrayList<String> ids = new ArrayList<>();
        int size = mContactAdapter.getSelectedItemCount();
        for (int i = size - 1; i >= 0; i--) {
            LinphoneContact contact = (LinphoneContact) objectsToDelete[i];
            if (contact.isAndroidContact()) {
                contact.deleteFriend();
                ids.add(contact.getAndroidId());
            } else {
                contact.delete();
            }
        }
        ContactsManager.getInstance().deleteMultipleContactsAtOnce(ids);
    }

    private void loadMoreContacts(String view, String search, boolean isInSearchMode) {
        isLoading = false;
        currentPage += 1;
        mContactsFetchInProgress.setVisibility(View.VISIBLE);
        searchContactsNethesis(
                view, this, search, LIMIT * currentPage, isInSearchMode, false, false);
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void searchContactsNethesis(
            final String view,
            ContactViewHolder.ClickListener clickListener,
            String search,
            int ofset,
            boolean isInSeachMode,
            boolean isRefreshing,
            boolean isFirst) {

        fetchContactsResponse(
                search,
                view,
                getContext(),
                ofset,
                clickListener,
                isInSeachMode,
                isRefreshing,
                isFirst);
    }

    public void setSelectView(String view) {
        mView = view;
        searchContacts(mSearchView.getQuery().toString());
    }

    private void fetchContactsResponse(
            String search,
            String view,
            Context context,
            final int offset,
            final ContactViewHolder.ClickListener clickListener,
            final boolean isInSeachMode,
            final boolean isRefreshing,
            final boolean isFirst) {

        if (offset == 0 && !ContactsManager.getInstance().getSIPContacts().isEmpty()) {
            ContactsManager.getInstance().getSIPContacts().clear();
        }
        listContact = ContactsManager.getInstance().getSIPContacts();

        String domain = SharedPreferencesManager.getDomain(context);
        String authToken = SharedPreferencesManager.getAuthtoken(context);

        UserRestAPI userRestAPI = RetrofitGenerator.createService(UserRestAPI.class, domain);
        Callback<ContactList> responseManagement =
                new Callback<ContactList>() {
                    @Override
                    public void onResponse(Call<ContactList> call, Response<ContactList> response) {
                        if (response.isSuccessful()) {
                            ContactList contactList = response.body();
                            if (contactList == null) {
                                return;
                            }
                            int oldRVPos = mLayoutManager.findFirstVisibleItemPosition();
                            ContactsManager.setMaximumNethesisContactCount(contactList.getCount());
                            List<Contact> contacts = contactList.getRows();
                            for (Contact c : contacts) {
                                NethesisContact contact =
                                        ContactsManager.getInstance()
                                                .createNethesisContactFromAPI(c);
                                ListIterator<LinphoneContact> iterator = listContact.listIterator();
                                boolean isPresent = false;
                                while (iterator.hasNext()) {
                                    LinphoneContact contact2 = iterator.next();
                                    if (contact2 instanceof NethesisContact
                                            && contact2.isNethesisContact()) {
                                        if (((NethesisContact) contact2).getId()
                                                == contact.getId()) {
                                            isPresent = true;
                                            break;
                                        }
                                    }
                                }
                                if (!isPresent) {
                                    listContact.add(contact);
                                }
                            }

                            if (mOnlyDisplayLinphoneContacts) {
                                mContactAdapter =
                                        new ContactsAdapter(
                                                mContext,
                                                listContact,
                                                clickListener,
                                                mSelectionHelper);
                                mContactAdapter.setIsSearchMode(isInSeachMode);

                                mSelectionHelper.setAdapter(mContactAdapter);
                                mContactsList.setAdapter(mContactAdapter);
                                // mContactAdapter.updateDataSet(listContact);

                                mNoSipContact.setVisibility(View.GONE);

                                if (!mOnlyDisplayLinphoneContacts
                                        && mContactAdapter.getItemCount() == 0) {
                                    mNoContact.setVisibility(View.VISIBLE);
                                } else if (mOnlyDisplayLinphoneContacts
                                        && mContactAdapter.getItemCount() == 0) {
                                    mNoSipContact.setVisibility(View.VISIBLE);
                                }
                                mContactsFetchInProgress.setVisibility(View.GONE);
                                mContactsRefresher.setRefreshing(false);

                                mContactAdapter.notifyDataSetChanged();

                                if (offset != 0) {
                                    mContactsList.scrollToPosition(oldRVPos);
                                }

                                if (isFirst && LinphoneActivity.instance().isTablet()) {
                                    displayFirstContact();
                                }
                            }
                        } else if (response.code() == 401) {
                            LinphoneManager.clearProxys(getContext());
                            startActivity(
                                    new Intent()
                                            .setClass(
                                                    LinphoneManager.getInstance().getContext(),
                                                    AssistantActivity.class));

                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ContactList> call, Throwable throwable) {}
                };
        Call<ContactList> searchCall;
        if (isInSeachMode) {
            searchCall = userRestAPI.searchStartsWith(authToken, search, offset, view);
        } else {
            searchCall = userRestAPI.searchWith(authToken, offset, view);
        }

        searchCall.enqueue(responseManagement);
    }

    private void sortContactByView(List<LinphoneContact> listContact) {
        boolean areAllNotNethesisContact = false;
        for (LinphoneContact contact : listContact) {
            if (!contact.isNethesisContact()) {
                areAllNotNethesisContact = true;
                break;
            }
        }
        if (!areAllNotNethesisContact) {
            switch (mView) {
                case "all":
                case "person":
                    Collections.sort(
                            listContact,
                            new Comparator() {

                                public int compare(Object o1, Object o2) {
                                    String x3 =
                                            ((NethesisContact) o1).getFullName() != null
                                                    ? ((NethesisContact) o1).getFullName()
                                                    : "";
                                    String x4 =
                                            ((NethesisContact) o2).getFullName() != null
                                                    ? ((NethesisContact) o2).getFullName()
                                                    : "";
                                    int asd = x3.compareTo(x4);

                                    if (asd != 0) {
                                        return asd;
                                    }

                                    String x1 =
                                            ((NethesisContact) o1).getOrganization() != null
                                                    ? ((NethesisContact) o1).getOrganization()
                                                    : "";
                                    String x2 =
                                            ((NethesisContact) o2).getOrganization() != null
                                                    ? ((NethesisContact) o2).getOrganization()
                                                    : "";
                                    return x1.compareTo(x2);
                                }
                            });
                    break;
                case "company":
                    break;
            }
        }
    }
}

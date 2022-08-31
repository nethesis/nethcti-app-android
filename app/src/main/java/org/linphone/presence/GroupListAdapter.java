package org.linphone.presence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.linphone.R;
import org.linphone.interfaces.OnNethGroupSelected;

import java.util.ArrayList;

import it.nethesis.models.NethPermissionWithOpGroups;


public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    public NethPermissionWithOpGroups _selectedUsersWithOpGroups;
    private ArrayList<NethPermissionWithOpGroups> associatedGroups;
    public OnNethGroupSelected onNethGroupSelected;

    public GroupListAdapter(
            ArrayList<NethPermissionWithOpGroups> _associatedGroups,
            NethPermissionWithOpGroups selected,
            OnNethGroupSelected onNethGroupSelected
    ) {
        this.associatedGroups = _associatedGroups;
        this.onNethGroupSelected = onNethGroupSelected;
        this._selectedUsersWithOpGroups = selected;
    }

    @NonNull
    @Override
    public GroupListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item, parent, false);

        return new GroupListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupListAdapter.ViewHolder holder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.setContent(associatedGroups.get(position));
    }

    @Override
    public int getItemCount() {
        return associatedGroups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txtGroupName;
        private NethPermissionWithOpGroups usersWithOpGroups;
        private Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            txtGroupName = itemView.findViewById(R.id.txt_group_name);
        }

        public void setContent(NethPermissionWithOpGroups _associatedGroup) {
            usersWithOpGroups = _associatedGroup;
            String groupName = (String) usersWithOpGroups.opGroupUsers.keySet().toArray()[0];
            txtGroupName.setText(groupName);
            txtGroupName.setOnClickListener(this);

            boolean isGroupSelected = false;

            String currentKey = _associatedGroup.opGroupUsers.keySet().toArray()[0].toString();
            String selectedKey = _selectedUsersWithOpGroups.opGroupUsers.keySet().toArray()[0].toString();
            isGroupSelected = currentKey.equals(selectedKey);
            txtGroupName.setTextColor(
                    isGroupSelected
                            ? ContextCompat.getColor(context, R.color.presence_selected_item)
                            : ContextCompat.getColor(context, R.color.presence_unselected_item)
            );
            txtGroupName.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    isGroupSelected ? R.drawable.ic_item_selected : 0,
                    0
            );
        }

        @Override
        public void onClick(View view) {
            /*if (usersWithOpGroups.equals(_selectedUsersWithOpGroups))
                _selectedUsersWithOpGroups = null;
            else
                _selectedUsersWithOpGroups = usersWithOpGroups;*/
            _selectedUsersWithOpGroups = usersWithOpGroups;
            onNethGroupSelected.onNethGroupSelected(_selectedUsersWithOpGroups);
        }
    }

}

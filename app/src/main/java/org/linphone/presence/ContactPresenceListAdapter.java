package org.linphone.presence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.linphone.R;

import java.util.ArrayList;
import it.nethesis.models.presence.PresenceUser;

public class ContactPresenceListAdapter extends RecyclerView.Adapter<ContactPresenceListAdapter.ViewHolder> {

    private ArrayList<PresenceUser> _presenceUserList = new ArrayList<PresenceUser>();

    public ContactPresenceListAdapter(ArrayList<PresenceUser> _presenceUserList) {
        this._presenceUserList = _presenceUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.presence_user_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.setContent(_presenceUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return _presenceUserList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setContent(PresenceUser presenceUser) {

        }

    }

}

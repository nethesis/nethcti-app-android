package org.linphone.presence;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.linphone.R;
import org.linphone.core.ProxyConfig;
import org.linphone.interfaces.OnAdapterItemListener;
import org.linphone.utils.SharedPreferencesManager;

import java.util.ArrayList;

import it.nethesis.models.decorator.MainPresenceDecorator;
import it.nethesis.models.presence.PresenceUser;

public class ContactPresenceListAdapter extends RecyclerView.Adapter<ContactPresenceListAdapter.ViewHolder> {

    private ProxyConfig _mProxyConfig;
    private ArrayList<PresenceUser> _presenceUserList = new ArrayList<PresenceUser>();
    private final OnAdapterItemListener onAdapterItemListener;

    public ContactPresenceListAdapter(
            ProxyConfig mProxyConfig,
            ArrayList<PresenceUser> _presenceUserList,
            OnAdapterItemListener onAdapterItemListener
    ) {
        this._mProxyConfig = mProxyConfig;
        this._presenceUserList = _presenceUserList;
        this.onAdapterItemListener = onAdapterItemListener;
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final View itemView;
        private final ImageView imgBackgroundCircleBorder;
        private final ImageView imgSmallStatusICon;
        private final TextView txtInitials;
        private final TextView txtPresenceStatus;
        private final TextView txtUsername;
        private final ImageView imgMenu;

        private PresenceUser presenceUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            txtUsername = itemView.findViewById(R.id.txt_username);
            txtInitials = itemView.findViewById(R.id.txt_user_initials);
            imgSmallStatusICon =  itemView.findViewById(R.id.img_small_status_icon);
            imgBackgroundCircleBorder =
                    itemView.findViewById(R.id.img_background_border_presence_status);
            txtPresenceStatus = itemView.findViewById(R.id.txt_status);
            imgMenu = itemView.findViewById(R.id.img_menu);
        }

        public void setContent(PresenceUser presenceUser) {
            this.presenceUser = presenceUser;

            boolean meSelf = SharedPreferencesManager.getUsername(itemView.getContext())
                    .equals(presenceUser.username);

            MainPresenceDecorator decorator;

            // Vedi se sono io... se sono io verifica disabled
            if (meSelf) {
                boolean disabled = !_mProxyConfig.registerEnabled();
                decorator = new MainPresenceDecorator(presenceUser.getMainPresence(disabled));
            } else {
                decorator = new MainPresenceDecorator(presenceUser.mainPresence);
            }


            txtUsername.setText(presenceUser.name);
            txtInitials.setText(presenceUser.getInitials());

            imgSmallStatusICon.setImageResource(decorator.getSmallCircleStatusIcon());
            imgBackgroundCircleBorder.setImageResource(decorator.getBackgroundCircleStatusIcon());

            txtPresenceStatus.setText(decorator.getStatusLabel());
            Drawable backgroundPresenceSquare = ContextCompat
                    .getDrawable(itemView.getContext(), decorator.getStatusSquareBackground());
            txtPresenceStatus.setBackground(backgroundPresenceSquare);
            txtPresenceStatus.setPadding(8, 3, 8, 3);

            if (amMeself()) {
                imgMenu.setVisibility(View.INVISIBLE);
                imgMenu.setOnClickListener(null);
            } else {
                imgMenu.setVisibility(View.VISIBLE);
                imgMenu.setOnClickListener(this);
            }

        }

        private boolean amMeself() {
            String loggedUsername = SharedPreferencesManager.getUsername(itemView.getContext());
            return presenceUser.username.equals(loggedUsername);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.img_menu) {
                if (amMeself()) return;
                onAdapterItemListener.onClickItem(presenceUser);
            }
        }
    }

}

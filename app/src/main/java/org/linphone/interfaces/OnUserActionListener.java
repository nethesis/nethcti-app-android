package org.linphone.interfaces;

import it.nethesis.models.NethPermission;
import it.nethesis.models.presence.PresenceUser;

public interface OnUserActionListener {
    public void onFavoritePressed(boolean isFavorite, PresenceUser presenceUser);
    public void onActionButtonPressed(int action, PresenceUser presenceUser);
}

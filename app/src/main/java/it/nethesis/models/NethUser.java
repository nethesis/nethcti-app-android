package it.nethesis.models;

import static it.nethesis.models.decorator.PresenceDecorator.PRESENCE_STATUS_DISABLED;

import androidx.annotation.Keep;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * It represent the user in NethCTI Server.
 * Comment fields are not used now, but are in the api response.
 */
@Keep
public class NethUser {
    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("username")
    @Expose
    public String username;

    @SerializedName("presence")
    private String presence;

    @SerializedName("mainPresence")
    public String mainPresence;

    @SerializedName("endpoints")
    @Expose
    public Endpoints endpoints;

    @SerializedName("profile")
    public NethProfile profile;

    @SerializedName("recallOnBusy")
    public String recallOnBusy;

    // @SerializedName("presenceOnBusy")
    // public String presenceOnBusy;

    // @SerializedName("presenceOnUnavailable")
    // public String presenceOnUnavailable;

    // @SerializedName("profile")
    // public Profile profile;

    // @SerializedName("default_device")
    // public DefaultDevice defaultDevice;

    // @SerializedName("settings")
    // public Settings_ settings;

    public String getPresence(boolean disabled) {
        return disabled ? PRESENCE_STATUS_DISABLED : presence;
    }

    private NethPermission getPermission(String permissionId) {
        return profile.macro_permissions
                .presence_panel
                .permissions
                .get(permissionId);
    }

    public boolean haveRecallOnBusyPermission() {
        if (recallOnBusy == null) return false;
        return this.recallOnBusy.equals("enabled");
    }

    public boolean havePermission(String permissionId) {
        NethPermission nethPermission = getPermission(permissionId);
        if (nethPermission == null) return false;
        if (nethPermission.name == null) return false;
        return nethPermission.name.equals(permissionId) && nethPermission.value;
    }

    public ArrayList<NethPermission> getNethPermissions() {
        ArrayList<NethPermission> groups = new ArrayList();
        for (Map.Entry<String, NethPermission> entry : profile.macro_permissions.presence_panel.permissions.entrySet()) {
            if (entry.getKey().contains("grp_")) {
                groups.add(entry.getValue());
            }

        }
        return groups;
    }

    //public static final int STATUS_NONE = -1;
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_BUSY = "busy";
    public static final String STATUS_RINGING = "ringing";
    public static final String STATUS_OFFLINE = "offline";
    public static final String STATUS_CELLPHONE = "cellphone";
    public static final String STATUS_VOICEMAIL = "voicemail";
    public static final String STATUS_DO_NOT_DISTURB = "dnd";
    public static final String STATUS_CALL_FORWARD = "callforward";
}

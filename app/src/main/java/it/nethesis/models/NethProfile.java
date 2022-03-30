package it.nethesis.models;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.gson.annotations.SerializedName;

public class NethProfile {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("macro_permissions")
    public NethMacroPermissions macro_permissions;

    public static boolean setActionButton(NethPermission nethPermission, String permission){
        if(nethPermission.name.equals(permission))
            return nethPermission.value;
        return false;
    }

    public static final String PERMISSION_INTRUDE = "intrude";
    public static final String PERMISSION_HANGUP = "hangup";
    public static final String PERMISSION_SPY = "spy";
    public static final String PERMISSION_PICKUP = "pickup";
    public static final String PERMISSION_RECORD = "ad_recording";
    public static final String PERMISSION_CLOSE = "hangup";

}

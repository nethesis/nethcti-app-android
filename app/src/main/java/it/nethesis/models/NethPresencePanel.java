package it.nethesis.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class NethPresencePanel {

    @SerializedName("value")
    public boolean value;

    @SerializedName("permissions")
    public HashMap<String, NethPermission> permissions;


}

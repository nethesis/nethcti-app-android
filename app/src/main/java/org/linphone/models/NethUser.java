package org.linphone.models;

import com.google.gson.annotations.SerializedName;

/*
 * It represent the user in NethCTI Server.
 * Comment fields are not used now, but are in the api response.
 */
public class NethUser {
    @SerializedName("name")
    public String name;

    @SerializedName("username")
    public String username;

    // @SerializedName("presence")
    // public String presence;

    @SerializedName("endpoints")
    public Endpoints endpoints;

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

}

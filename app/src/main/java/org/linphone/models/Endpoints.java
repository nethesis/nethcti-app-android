package org.linphone.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/*
 * It represent the endpoints for NethUsers.
 * Comment fields are not used now, but are in the api response.
 */
public class Endpoints {
    // @SerializedName("email")
    // public List<Email> email = null;

    // @SerializedName("jabber")
    // public List<Jabber> jabber = null;

    @SerializedName("extension")
    @Expose
    public List<Extension> extension = null;

    // @SerializedName("cellphone")
    // public List<Object> cellphone = null;

    // @SerializedName("voicemail")
    // public List<Object> voicemail = null;

    // @SerializedName("mainextension")
    // public List<Mainextension> mainextension = null;
}

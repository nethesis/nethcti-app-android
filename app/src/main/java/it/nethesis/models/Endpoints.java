package it.nethesis.models;


import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.linphone.R;

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

    @SerializedName("mainextension")
    public List<Extension> mainextension = null;


    private Extension getFirstMainExtension() {
        if (mainextension == null || mainextension.isEmpty()) return null;
        return mainextension.get(0);
    }

    public String getMainExtensionId() {
        if (getFirstMainExtension() != null)
            return getFirstMainExtension().id;
        return null;
    }

    public String getMainExtensionLocalizedId(Context context) {
        String nd = context.getString(R.string.presence_satus_nd).toLowerCase();
        Extension main = getFirstMainExtension();
        if (main == null) return nd;
        return main.id == null || main.id.isEmpty() ? nd : main.id;
    }

    private Extension getFirstExtension() {
        if (extension == null || extension.isEmpty()) return null;
        return extension.get(0);
    }

    public String getFirstExtensionId() {
        if (getFirstExtension() != null)
            return getFirstExtension().id;
        return null;
    }

    public Extension getExtensionWithMobileType() {
        for (Extension extension : this.extension) {
            if (extension.type.equals(TYPE_MOBILE))
                return extension;
        }
        return null;
    }

    public String getMobileExtensionId() {
        return getExtensionWithMobileType().id;
    }

    public static String TYPE_MOBILE = "mobile";

}

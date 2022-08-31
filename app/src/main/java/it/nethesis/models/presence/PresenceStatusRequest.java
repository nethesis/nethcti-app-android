package it.nethesis.models.presence;

import com.google.gson.annotations.SerializedName;

public class PresenceStatusRequest {

    @SerializedName("status")
    public String status = "";

    public PresenceStatusRequest(
            String _status
    ) {
        this.status = _status;
    }

}

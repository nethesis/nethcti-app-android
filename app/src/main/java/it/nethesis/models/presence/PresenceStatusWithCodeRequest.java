package it.nethesis.models.presence;

import com.google.gson.annotations.SerializedName;

public class PresenceStatusWithCodeRequest {

    @SerializedName("status")
    public String status = "";

    @SerializedName("to")
    public String to = null;

    public PresenceStatusWithCodeRequest(
            String _status,
            String _to
    ) {
        this.status = _status;
        this.to = _to;
    }

}

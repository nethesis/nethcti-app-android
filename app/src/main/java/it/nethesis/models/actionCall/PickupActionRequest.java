package it.nethesis.models.actionCall;

import com.google.gson.annotations.SerializedName;

public class PickupActionRequest {

    @SerializedName("endpointId")
    private String endpointId;

    @SerializedName("destId")
    private String destId;

    public PickupActionRequest(String _endpointId, String _destId) {
        this.setEndpointId(_endpointId);
        this.setDestId(_destId);
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }
}

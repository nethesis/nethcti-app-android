package it.nethesis.models.actionCall;

import com.google.gson.annotations.SerializedName;

public class RedirectRequest {
    @SerializedName("convid")
    private String convid;

    @SerializedName("endpointId")
    private String endpointId;

    @SerializedName("destId")
    private String destId;

    public RedirectRequest(String _convid, String _endpointId, String _destId) {
        this.setConvid(_convid);
        this.setEndpointId(_endpointId);
        this.setDestId(_destId);
    }

    public String getConvid() {
        return convid;
    }

    public void setConvid(String convid) {
        this.convid = convid;
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

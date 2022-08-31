package it.nethesis.models.actionCall;

import com.google.gson.annotations.SerializedName;

public class HangupActionRequest {

    @SerializedName("convid")
    private String convid;

    //L'id di chi deve chiudere la chiamata (chi esegue l'azione)
    @SerializedName("endpointId")
    private String endpointId;

    public HangupActionRequest(String _convid, String _endpointId){
        this.setConvid(_convid);
        this.setEndpointId(_endpointId);
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
}

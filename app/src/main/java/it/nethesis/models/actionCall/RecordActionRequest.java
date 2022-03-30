package it.nethesis.models.actionCall;

import com.google.gson.annotations.SerializedName;

public class RecordActionRequest {
    @SerializedName("convid")
    private String convid;

    @SerializedName("endpointType")
    private String endpointType;

    @SerializedName("endpointId")
    private String endpointId;

    public RecordActionRequest(String _convid, String _endpointType, String _endpointId){
        this.setConvid(_convid);
        this.setEndpointType(_endpointType);
        this.setEndpointId(_endpointId);
    }

    public String getConvid() {
        return convid;
    }

    public void setConvid(String convid) {
        this.convid = convid;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }
}

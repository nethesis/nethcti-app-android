package it.nethesis.models.notificatore;

import com.google.gson.annotations.SerializedName;

public class RegisterTokenReponse {
    @SerializedName("ResultCode")
    private String ResultCode;

    @SerializedName("ResultMessage")
    private String ResultMessage;

    public String getResultCode() {
        return ResultCode;
    }

    public void setResultCode(String resultCode) {
        ResultCode = resultCode;
    }

    public String getResultMessage() {
        return ResultMessage;
    }

    public void setResultMessage(String resultMessage) {
        ResultMessage = resultMessage;
    }
}

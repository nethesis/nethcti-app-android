package it.nethesis.models.notificatore;

import com.google.gson.annotations.SerializedName;

public class RegisterToken {
    @SerializedName("User")
    private String user;

    @SerializedName("Os")
    private String os;

    @SerializedName("DevToken")
    private String devToken;

    @SerializedName("RegID")
    private String regID;

    @SerializedName("Language")
    private String language;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getDevToken() {
        return devToken;
    }

    public void setDevToken(String devToken) {
        this.devToken = devToken;
    }

    public String getRegID() {
        return regID;
    }

    public void setRegID(String regID) {
        this.regID = regID;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    @SerializedName("Custom")
    private String custom;
}

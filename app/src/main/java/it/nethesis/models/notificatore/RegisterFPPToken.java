package it.nethesis.models.notificatore;

import com.google.gson.annotations.SerializedName;

public class RegisterFPPToken {
    @SerializedName("token")
    private String token;

    @SerializedName("topic")
    private String topic;

    @SerializedName("type")
    private String type;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

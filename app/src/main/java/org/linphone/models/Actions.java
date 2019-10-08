package org.linphone.models;

import com.google.gson.annotations.SerializedName;

/*
 * It represent the actions for Extensions.
 * Comment fields are not used now, but are in the api response.
 */
public class Actions {
    @SerializedName("answer")
    public Boolean answer;

    @SerializedName("dtmf")
    public Boolean dtmf;

    @SerializedName("hold")
    public Boolean hold;
}

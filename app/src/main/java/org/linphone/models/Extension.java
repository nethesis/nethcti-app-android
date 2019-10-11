package org.linphone.models;

import com.google.gson.annotations.SerializedName;

/*
 * It represent the extension in Endpoints.
 * Comment fields are not used now, but are in the api response.
 */
public class Extension {

    @SerializedName("id")
    public String id;

    @SerializedName("type")
    public String type;

    @SerializedName("secret")
    public String secret;

    @SerializedName("username")
    public String username;

    @SerializedName("description")
    public String description;

    @SerializedName("actions")
    public Actions actions;
}

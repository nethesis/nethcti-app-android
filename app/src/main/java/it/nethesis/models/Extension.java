package it.nethesis.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

/*
 * It represent the extension in Endpoints.
 * Comment fields are not used now, but are in the api response.
 */
public class Extension {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("type")
    @Expose
    public String type;

    @SerializedName("secret")
    @Expose
    public String secret;

    @SerializedName("username")
    @Expose
    public String username;

    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("proxy_port")
    @Expose
    @Nullable
    public Integer proxyPort = null;
}

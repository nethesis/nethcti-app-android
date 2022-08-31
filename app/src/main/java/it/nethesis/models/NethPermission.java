package it.nethesis.models;

import com.google.gson.annotations.SerializedName;

public class NethPermission {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name = "";

    @SerializedName("value")
    public boolean value = false;
}

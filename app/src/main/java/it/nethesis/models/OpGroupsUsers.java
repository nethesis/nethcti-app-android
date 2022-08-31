package it.nethesis.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OpGroupsUsers {
    @SerializedName("users")
    public ArrayList<String> users = new ArrayList();
}

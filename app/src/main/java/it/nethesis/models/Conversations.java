package it.nethesis.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class Conversations {

    @SerializedName("counterparconversationtName")
    public HashMap<String, Conversation> conversation;

    public Conversations(HashMap<String, Conversation> conversation) {
        this.conversation = conversation;
    }

}

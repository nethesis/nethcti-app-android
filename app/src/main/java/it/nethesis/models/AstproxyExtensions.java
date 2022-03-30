package it.nethesis.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/*
 * It represent the extension in astproxy/extensions.
 */
public class AstproxyExtensions {

    @SerializedName("ip")
    public String ip = "";

    @SerializedName("mac")
    public String mac = "";

    @SerializedName("dnd")
    public boolean dnd = false;

    @SerializedName("port")
    public String port = "";

    @SerializedName("name")
    public String name = "";

    @SerializedName("exten")
    public String exten = "";

    @SerializedName("conversations")
    public HashMap<String, Conversation> conversations;

    public AstproxyExtensions(
            String ip,
            String mac,
            boolean dnd,
            String port,
            String name,
            String exten,
            HashMap<String, Conversation> conversations
    ) {
        this.ip = ip;
        this.mac = mac;
        this.dnd = dnd;
        this.port = port;
        this.name = name;
        this.exten = exten;
        this.conversations = conversations;
    }

}

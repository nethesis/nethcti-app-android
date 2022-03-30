package it.nethesis.models;

import com.google.gson.annotations.SerializedName;

public class Conversation {

    @SerializedName("id")
    public String id = "";

    @SerializedName("owner")
    public String owner = "";

    @SerializedName("queueId")
    public String queueId = ""; // the queue identifier if the conversation has gone through a queue

    @SerializedName("duration")
    public int duration = 0;

    @SerializedName("recording")
    public String recording = ""; // it's "true" or "mute" if the conversation is recording, "false" otherwise

    @SerializedName("direction")
    public String direction = "";

    @SerializedName("inConference")
    public boolean inConference = false; // if the conversation involves a meetme conference

    @SerializedName("throughQueue")
    public boolean throughQueue = false; // if the call has gone through a queue

    @SerializedName("counterpartNum")
    public String counterpartNum = "";

    @SerializedName("counterpartName")
    public String counterpartName = "";

    public Conversation(
            String id,
            String owner,
            String queueId,
            int duration,
            String recording,
            String direction,
            boolean inConference,
            boolean throughQueue,
            String counterpartNum,
            String counterpartName
    ) {
        this.id = id;
        this.owner = owner;
        this.queueId = queueId;
        this.duration = duration;
        this.recording = recording;
        this.direction = direction;
        this.inConference = inConference;
        this.throughQueue = throughQueue;
        this.counterpartNum = counterpartNum;
        this.counterpartName = counterpartName;
    }

}

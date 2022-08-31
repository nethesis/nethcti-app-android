package it.nethesis.models.actionCall;

import com.google.gson.annotations.SerializedName;

public class CallerAndCalled {
    @SerializedName("caller")
    private String caller;

    @SerializedName("called")
    private String called;

    public CallerAndCalled(String _caller, String _called){
        this.setCaller(_caller);
        this.setCalled(_called);
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCalled() {
        return called;
    }

    public void setCalled(String called) {
        this.called = called;
    }
}

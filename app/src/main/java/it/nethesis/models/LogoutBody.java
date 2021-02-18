package it.nethesis.models;

import androidx.annotation.NonNull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** The Logout Body. */
public class LogoutBody {

    @SerializedName("type")
    @Expose
    private String Type;

    /**
     * Set the type.
     */
    this.Type = "mobile";

}

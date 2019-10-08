package org.linphone.models;

import com.google.gson.annotations.SerializedName;

public class LoginCredentials {
    @SerializedName("username")
    private String Username;

    @SerializedName("password")
    private String Password;

    public void setUsername(String username) {
        this.Username = username;
    }

    public void setPassword(String password) {
        this.Password = password;
    }

    public String getUsername() {
        return this.Username;
    }

    public String getPassword() {
        return this.Password;
    }

    public LoginCredentials(String username, String password) {
        this.setUsername(username);
        this.setPassword(password);
    }
}

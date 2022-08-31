package it.nethesis.models;

import androidx.annotation.NonNull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** The Login Credentials. */
public class LoginCredentials {
    @SerializedName("username")
    @Expose
    private String Username;

    @SerializedName("password")
    @Expose
    private String Password;

    /**
     * Set the username.
     *
     * @param username The username.
     */
    public void setUsername(String username) {
        this.Username = username;
    }

    /**
     * Set the password.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.Password = password;
    }

    /**
     * Get the username.
     *
     * @return The username.
     */
    public String getUsername() {
        return this.Username;
    }

    /**
     * Get the password.
     *
     * @return The password.
     */
    public String getPassword() {
        return this.Password;
    }

    /**
     * Instantiates a new Login Credentials.
     *
     * @param username The username.
     * @param password The password.
     */
    public LoginCredentials(String username, String password) {
        this.setUsername(username);
        this.setPassword(password);
    }

    @NonNull
    @Override
    public String toString() {
        return "Username: " + this.getUsername() + " Password: " + this.getPassword();
    }
}

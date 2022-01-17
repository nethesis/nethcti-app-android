package it.nethesis.webservices;

import java.util.HashMap;

import it.nethesis.models.presence.PresenceUser;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

/** Interface for Presence Rest Api Calls. */
public interface PresenceRestAPI {

    /**
     * Perform the GET user/endpoints/all.
     *
     * @param authorizationHeader username plus digest
     * @return
     */
    @GET("user/endpoints/all")
    Call<HashMap<String, PresenceUser>> getAll(@Header("Authorization") String authorizationHeader);

}

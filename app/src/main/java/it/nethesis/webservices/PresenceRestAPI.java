package it.nethesis.webservices;

import java.util.HashMap;

import it.nethesis.models.OpGroupsUsers;
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
    Call<HashMap<String, PresenceUser>> getAllPresenceUsers(@Header("Authorization") String authorizationHeader);

    /**
     * Perform the GET astproxy/opgroups.
     *
     * @param authorizationHeader username plus digest
     * @return
     */
    @GET("astproxy/opgroups")
    Call<HashMap<String, OpGroupsUsers>> getAllPresenceGroups(@Header("Authorization") String authorizationHeader);

}

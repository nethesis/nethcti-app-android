package it.nethesis.webservices;

import java.util.List;

import it.nethesis.models.NethUser;
import it.nethesis.models.contactlist.ContactList;
import it.nethesis.models.notificatore.RegisterToken;
import it.nethesis.models.notificatore.RegisterTokenReponse;
import it.nethesis.models.presence.PresenceStatusRequest;
import it.nethesis.models.presence.PresenceStatusWithCodeRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Interface for Users Rest Api Calls. */
public interface UserRestAPI {
    /**
     * Perform the GET me call.
     *
     * @param authorizationHeader username plus digest
     * @return
     */
    @GET("user/me")
    Call<NethUser> getMe(@Header("Authorization") String authorizationHeader);

    @POST("user/presence")
    Call<ResponseBody> setPresence(
            @Header("Authorization") String authorizationHeader,
            @Body PresenceStatusRequest presenceStatus
    );

    @POST("user/presence")
    Call<ResponseBody> setPresence(
            @Header("Authorization") String authorizationHeader,
            @Body PresenceStatusWithCodeRequest presenceStatusWithCode
    );

    @GET("user/presencelist")
    Call<List<String>> getPresenceStatusList(@Header("Authorization") String authorizationHeader);

    @GET("phonebook/search/{term}")
    Call<ContactList> searchStartsWith(
            @Header("Authorization") String authorizationHeader,
            @Path("term") String term,
            @Query("offset") int offset,
            @Query("limit") int limit,
            @Query("view") String view);

    @GET("phonebook/getall/")
    Call<ContactList> getAll(
            @Header("Authorization") String authorizationHeader,
            @Query("offset") int offset,
            @Query("limit") int limit);
    /*@Query("view") String view);*/
}

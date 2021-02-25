package it.nethesis.webservices;

import it.nethesis.models.NethUser;
import it.nethesis.models.contactlist.ContactList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    @GET("phonebook/searchstartswith/{term}?")
    Call<ContactList> searchStartsWith(
            @Header("Authorization") String authorizationHeaderaaaa,
            @Path("term") String term,
            @Query("offset") int offset,
            @Query("limit") int limit,
            @Query("view") String view);

    @GET("phonebook/searchstartswith/{term}?limit=50")
    Call<ContactList> searchStartsWith(
            @Header("Authorization") String authorizationHeader,
            @Path("term") String term,
            @Query("offset") int offset,
            @Query("view") String view);
}

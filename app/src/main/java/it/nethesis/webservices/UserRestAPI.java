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

    @GET("phonebook/search/{term}?view=name")
    Call<ContactList> searchWithTerms(
            @Header("Authorization") String authorizationHeader,
            @Path("term") String term,
            @Query("limit") int limit,
            @Query("offset") int offset);

    @GET("phonebook/search/{term}?view=name&limit=5")
    Call<ContactList> searchWithTerms(
            @Header("Authorization") String authorizationHeader,
            @Path("term") String term,
            @Query("offset") int offset);
}

package org.linphone.utils.webservices;

import org.linphone.models.NethUser;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

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
}

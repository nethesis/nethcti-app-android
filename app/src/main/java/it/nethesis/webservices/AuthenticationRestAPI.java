package it.nethesis.webservices;

import it.nethesis.models.LoginCredentials;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** Interface for Authentication Rest Api Calls. */
public interface AuthenticationRestAPI {
    /**
     * The login call. It return every time a 401 HTTP result.
     *
     * @param credentials Username and Password.
     * @return401 Unauthorized HTTP result. Important Digest in the header.
     */
    @POST("authentication/login")
    Call<String> login(@Body LoginCredentials credentials);

    /**
     * The logout call.
     *
     * @return 200 Ok HTTP Result.
     */
    @POST("authentication/logout")
    Call<String> logout();
}

package it.nethesis.webservices;

import it.nethesis.models.notificatore.RegisterFPPToken;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/** Interface for Notificatore Rest Api Calls. */
public interface NotificatoreRestAPI {
    @Headers({"Content-Type: application/json"})
    @POST("register")
    Call<ResponseBody> registerFPPToken(
            @Header("Instance-Token") String authKey, @Body RegisterFPPToken registerData);

    @Headers({"Content-Type: application/json"})
    @POST("deregister")
    Call<ResponseBody> deregisterFPPToken(
            @Header("Instance-Token") String authKey, @Body RegisterFPPToken registerData);
}

package it.nethesis.webservices;

import java.util.HashMap;

import it.nethesis.models.AstproxyExtensions;
import it.nethesis.models.actionCall.CallerAndCalled;
import it.nethesis.models.actionCall.RedirectRequest;
import it.nethesis.models.actionCall.HangupActionRequest;
import it.nethesis.models.actionCall.PickupActionRequest;
import it.nethesis.models.actionCall.RecordActionRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ActionCallRestAPI {

    @GET("astproxy/extensions")
    Call<HashMap<String, AstproxyExtensions>>  getExtensions(
        @Header("Authorization") String authorizationHeader
    );

    @POST("astproxy/recall_on_busy")
    Call<ResponseBody> recallOnBusy(
        @Header("Authorization") String authorizationHeader,
        @Body CallerAndCalled callerAndCalled
    );

    @POST("astproxy/start_spy")
    Call<ResponseBody> startSpy(
        @Header("Authorization") String authorizationHeader,
        @Body RedirectRequest redirectRequest
    );

    @POST("astproxy/intrude")
    Call<ResponseBody> intrude(
        @Header("Authorization") String authorizationHeader,
        @Body RedirectRequest redirectRequest
    );

    @POST("astproxy/unmute_record")
    Call<ResponseBody> unmuteRecord(
        @Header("Authorization") String authorizationHeader,
        @Body RecordActionRequest recordActionRequest
    );

    @POST("astproxy/mute_record")
    void muteRecord(
        @Header("Authorization") String authorizationHeader,
        @Body RecordActionRequest recordActionRequest
    );

    @POST("astproxy/pickup_conv")
    Call<ResponseBody> pickup(
        @Header("Authorization") String authorizationHeader,
        @Body PickupActionRequest pickupActionRequest
    );

    @POST("astproxy/hangup")
    Call<ResponseBody> hangup(
        @Header("Authorization") String authorizationHeader,
        @Body HangupActionRequest hangupActionRequest
    );
}

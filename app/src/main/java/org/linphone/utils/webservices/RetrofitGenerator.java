package org.linphone.utils.webservices;

import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** The Retrofit Service Generator. */
public class RetrofitGenerator {
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static final String baseUrl = "https://nethctiapp.nethserver.net/webrest/";

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(
                            GsonConverterFactory.create(
                                    new GsonBuilder().serializeNulls().create()));

    private static Retrofit retrofit;

    /**
     * Allow to create a service.
     *
     * @param <S> the type parameter.
     * @param serviceClass the service class.
     * @return the service.
     */
    public static <S> S createService(Class<S> serviceClass) {
        if (retrofit == null) {
            builder.client(httpClient.build());
        }
        retrofit = builder.build();
        return retrofit.create(serviceClass);
    }
}

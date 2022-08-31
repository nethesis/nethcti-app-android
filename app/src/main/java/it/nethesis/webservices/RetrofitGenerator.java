package it.nethesis.webservices;

import android.util.Log;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** The Retrofit Service Generator. */
public class RetrofitGenerator {
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static final String BASE_URL = "https://nethctiapp.nethserver.net/webrest/";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static Retrofit retrofit;

    /**
     * Allow to create a service.
     *
     * @param <S> the type parameter.
     * @param serviceClass the service class.
     * @return the service.
     */
    public static <S> S createService(Class<S> serviceClass, String domain, boolean interceptors) {
        String endpoint = domain == null ? BASE_URL : String.format("https://%s/webrest/", domain);

        if (retrofit == null || !retrofit.baseUrl().toString().equals(endpoint)) {
            Interceptor asd =
                    new Interceptor() {
                        @NotNull
                        @Override
                        public Response intercept(@NotNull Chain chain) throws IOException {
                            Request request = chain.request();
                            request = request.newBuilder().header("Auth-Exp", "no-exp").build();
                            return chain.proceed(request);
                        }
                    };
            httpClient.addNetworkInterceptor(asd);

            if (interceptors) {
                HttpLoggingInterceptor logBody =
                        new HttpLoggingInterceptor(
                                new HttpLoggingInterceptor.Logger() {
                                    @Override
                                    public void log(@NotNull String s) {
                                        Log.w("OK_HTTP_BODY", s);
                                    }
                                });
                logBody.level(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(logBody);
            }
            if (interceptors) {
                HttpLoggingInterceptor logHeader =
                        new HttpLoggingInterceptor(
                                new HttpLoggingInterceptor.Logger() {
                                    @Override
                                    public void log(@NotNull String s) {
                                        Log.w("OK_HTTP_HEADER", s);
                                    }
                                });
                logHeader.level(HttpLoggingInterceptor.Level.HEADERS);
                httpClient.addInterceptor(logHeader);
            }

            retrofit =
                    new Retrofit.Builder()
                            .baseUrl(endpoint)
                            .addConverterFactory(
                                    GsonConverterFactory.create(
                                            new GsonBuilder()
                                                    //.registerTypeAdapter(NethUser.class, new NethUserSerializer())
                                                    .serializeNulls().create()
                                    )
                            )
                            .client(httpClient.build())
                            .build();
        }

        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String domain) {
        return createService(serviceClass, domain, true);
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static String calculateRFC2104HMAC(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey =
                new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }
}

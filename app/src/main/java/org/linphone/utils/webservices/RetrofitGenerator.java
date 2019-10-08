package org.linphone.utils.webservices;

import com.google.gson.GsonBuilder;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** The Retrofit Service Generator. */
public class RetrofitGenerator {
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static final String baseUrl = "https://nethctiapp.nethserver.net/webrest/";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

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

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static String calculateRFC2104HMAC(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }
}

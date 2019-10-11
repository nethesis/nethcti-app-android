package org.linphone.utils.webservices;

import android.util.Log;
import android.widget.Toast;
import com.google.gson.GsonBuilder;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Formatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.linphone.assistant.AssistantActivity;
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
    public static <S> S createService(Class<S> serviceClass) {
        if (retrofit == null) {
            HttpLoggingInterceptor logBody =
                    new HttpLoggingInterceptor(
                            new HttpLoggingInterceptor.Logger() {
                                @Override
                                public void log(@NotNull String s) {
                                    Log.w("OK_HTTP_BODY", s);
                                }
                            });
            logBody.setLevel(HttpLoggingInterceptor.Level.BODY);
            HttpLoggingInterceptor logHeader =
                    new HttpLoggingInterceptor(
                            new HttpLoggingInterceptor.Logger() {
                                @Override
                                public void log(@NotNull String s) {
                                    Log.w("OK_HTTP_HEADER", s);
                                }
                            });
            logHeader.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            httpClient.addInterceptor(logHeader);
            httpClient.addInterceptor(logBody);
            retrofit =
                    new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(
                                    GsonConverterFactory.create(
                                            new GsonBuilder().serializeNulls().create()))
                            .client(httpClient.build())
                            .build();
        }

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

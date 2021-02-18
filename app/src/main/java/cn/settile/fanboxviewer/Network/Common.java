package cn.settile.fanboxviewer.Network;

import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxAPI;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import static cn.settile.fanboxviewer.App.getContext;
import static java.util.Objects.requireNonNull;

public class Common {
    public static OkHttpClient client = null;
    public static Picasso singleton = null;
    public static JSONObject userInfo = null;
    private static String TAG = "NetworkCommon";
    private static File cacheDir = null;

    public static Future<Boolean> isLoggedIn() {
        return Executors.newSingleThreadExecutor()
                .submit(() -> {
                    if (client == null) {
                        Log.d(TAG, "client is not defined!");
                        return false;
                    }
                    Request req = new Request.Builder()
                            .url("https://api.fanbox.cc/bell.countUnread")
                            .build();
                    try (Response resp = client.newCall(req).execute()) {
                        JSONObject json = new JSONObject(resp.body().string());
                        return Objects.equals(json.optString("error"), "general_error");
                    } catch (Exception ex) {
                        Log.e(TAG, "EXCEPTION: ", ex);
                        return false;
                    }
                });
    }

    public static void initClient() {
        if (!Objects.equals(client, null)) {
            return;
        }
        Cache cache = new Cache(getContext().getCacheDir(), 1024 * 1024 * 8);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        Common.client = new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieHandler())
                .cache(cache)
                .readTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        OkHttpClient pclient = new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieHandler())
                .addInterceptor(chain -> {
                    Request.Builder auth = chain.request().newBuilder();
                    auth.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36");
                    auth.addHeader("Origin", "https://www.fanbox.cc");
                    Response resp = chain.proceed(auth.build());
                    return resp;
                })
                .build();
        Retrofit fanbox = new Retrofit.Builder()
                .client(pclient)
                .baseUrl("https://api.fanbox.cc/")
                .build();

        FanboxAPI api = fanbox.create(FanboxAPI.class);
        FanboxParser.client = api;
    }

    public static void downloadThread(String url, File file, Runnable success, Runnable fail) {
        new Thread(() -> download(url, file, success, fail)).start();
    }

    public static boolean download(String url, File output, Runnable success, Runnable fail) {
        if (client == null) {
            Log.d(TAG, "client is not defined!");
            initClient();
            return download(url, output, success, fail);
        }
        URLRequestor ur = new URLRequestor(url, (it) -> {
            try {
                Log.d(TAG, "Downloading:"+url);
                InputStream is = it.body().byteStream();

                BufferedInputStream bif = new BufferedInputStream(is);
                OutputStream op = new FileOutputStream(output);

                byte[] buf = new byte[1024];
                int count;

                while ((count = bif.read(buf)) != -1) {
                    op.write(buf, 0, count);
                }

                op.flush();
                op.close();
                is.close();
            } catch (Exception e) {
                Log.e(TAG, "download: EXCEPTION", e);
                fail.run();
                return false;
            }
            success.run();
            return true;
        });


        return ur.getReturnValue();
    }
}

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
import java.util.concurrent.TimeUnit;

import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxAPI;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxUserParser;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import static cn.settile.fanboxviewer.App.getContext;

public class Common {
    public static Picasso singleton = null;
    public static JSONObject userInfo = null;


    private static String TAG = "NetworkCommon";
    private static File cacheDir = null;

    public static URLRequestor<Boolean> isLoggedIn(URLRequestor.OnDoneListener<Boolean> onDone) {
        return new URLRequestor<>("https://api.fanbox.cc/bell.countUnread", (it) -> {
            try {
                JSONObject json = new JSONObject(it.body().string());
                return Objects.equals(json.optString("error"), "general_error");
            } catch (Exception ex) {
                Log.e(TAG, "EXCEPTION: ", ex);
                return false;
            }
        }, null, onDone).async();
    }

    public static OkHttpClient getClientInstance() {
        return ClientHolder.clientInstance;
    }

    public static OkHttpClient initClient() {
        if (!Objects.equals(ClientHolder.clientInstance, null)) {
            return getClientInstance();
        }
        Cache cache = new Cache(getContext().getCacheDir(), 1024 * 1024 * 8);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
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
        FanboxUserParser.client = api;
        return client;
    }

    public static void downloadThread(String url, File file, Runnable success, Runnable fail) {
        new Thread(() -> download(url, file, success, fail)).start();
    }

    public static boolean download(String url, File output, Runnable success, Runnable fail) {
        URLRequestor<Boolean> ur = new URLRequestor<>(url, (it) -> {
            try {
                Log.d(TAG, "Downloading:" + url);
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
        },null,null).sync();


        return ur.getReturnValue();
    }

    public void getClientInstanceAsync(OnClientInitialedListener listener) {
        Thread th = new Thread(() -> {
            OkHttpClient client = getClientInstance();
            listener.onInitialed(client);
        });
        th.start();
    }

    interface OnClientInitialedListener {
        void onInitialed(OkHttpClient client);
    }

    private static class ClientHolder {
        public static OkHttpClient clientInstance = initClient();
    }

}

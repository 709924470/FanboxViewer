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

import cn.settile.fanboxviewer.Util.Constants;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.settile.fanboxviewer.App.getContext;

public class Common {
    public static OkHttpClient client = null;
    public static Picasso singleton = null;
    private static String TAG = "NetworkCommon";
    private static File cacheDir = null;

    public static JSONObject userInfo = null;

    public static Future<Boolean> isLoggedIn() {
        return Executors.newSingleThreadExecutor()
                .submit(() -> {
            if (client == null){
                Log.d(TAG, "client is not defined!");
                return false;
            }
            Request req = new Request.Builder()
                    .url("https://fanbox.pixiv.net/api/bell.countUnread")
                    .build();
            try(Response resp = client.newCall(req).execute()) {
                JSONObject json = new JSONObject(resp.body().string());
                boolean result = Objects.equals(json.getString("error"), "general_error");
                return result;
            }catch (Exception ex){
                Log.e(TAG, "EXCEPTION: ", ex);
                return false;
            }
        });
    }

    public static void initClient(){
        if (!Objects.equals(client, null)){
            return;
        }
        Cache cache = new Cache(getContext().getCacheDir(), 1024 * 1024 * 8);
        Common.client = new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieHandler())
                .cache(cache)
                .readTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
                    final Request orig = chain.request();
                    final Request withCookie = orig.newBuilder()
                            .addHeader("Cookie", Constants.Cookie).build();
                    return chain.proceed(withCookie);
                })
                .build();Common.client = new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieHandler())
                .cache(cache)
                .readTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
                    final Request orig = chain.request();
                    final Request withCookie = orig.newBuilder()
                            .addHeader("Cookie", Constants.Cookie).build();
                    return chain.proceed(withCookie);
                })
                .build();
    }

    public static void downloadThread(String url, File file, Runnable success, Runnable fail){
        new Thread(() -> download(url, file, success, fail)).start();
    }

    public static boolean download(String url, File output, Runnable success, Runnable fail){
        if (client == null){
            Log.d(TAG, "client is not defined!");
            initClient();
            if(download(url, output, success, fail)){
                return true;
            }
            fail.run();
            return false;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = client.newCall(request).execute()){
            InputStream is = response.body().byteStream();

            BufferedInputStream bif = new BufferedInputStream(is);
            OutputStream op = new FileOutputStream(output);

            byte[] buf = new byte[1024];
            int count;

            while((count = bif.read(buf)) != -1){
                op.write(buf, 0, count);
            }

            op.flush();
            op.close();
            is.close();
        }catch (Exception e){
            Log.e(TAG, "download: EXCEPTION", e);
            fail.run();
            return false;
        }
        success.run();
        return true;
    }
}

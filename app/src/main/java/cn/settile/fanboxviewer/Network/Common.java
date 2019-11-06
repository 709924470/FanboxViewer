package cn.settile.fanboxviewer.Network;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Common {
    public static OkHttpClient client = null;
    private static String TAG = "NetworkCommon";

    public static JSONObject userInfo = null;

    public static Future<Boolean> isLoggedIn() {
        return Executors.newSingleThreadExecutor()
                .submit(() -> {
            if (client == null){
                Log.d(TAG, "client is not defined!");
                return false;
            }
            Request req = new Request.Builder()
                    .url("https://www.pixiv.net/ajax/fanbox/notification/unread_count")
                    .build();
            try(Response resp = client.newCall(req).execute()) {
                JSONObject json = new JSONObject(resp.body().string());
                boolean result = !json.getBoolean("error");
                return result;
            }catch (Exception ex){
                Log.e(TAG, "EXCEPTION: ", ex);
                return false;
            }
        });
    }

    public static Bitmap download(String url, ProgressBar progress){
        if (client == null){
            Log.d(TAG, "client is not defined!");
            return null;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();
        try(Response response = client.newCall(request).execute()){

        }catch (Exception e){

        }
        return null;
    }
}

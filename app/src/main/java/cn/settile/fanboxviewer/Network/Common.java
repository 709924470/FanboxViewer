package cn.settile.fanboxviewer.Network;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ProgressBar;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Common {
    public static OkHttpClient cl = null;
    private static String TAG = "NetworkCommon";

    public static boolean isLoggedIn() {
        if (cl == null){
            Log.d(TAG, "client is not defined!");
            return false;
        }
        String url = "https://www.pixiv.net/ajax/fanbox/notification/unread_count";
        Request req = new Request.Builder()
                .url(url)
                .build();
        try(Response res = cl.newCall(req).execute()){
            JSONObject json = new JSONObject(res.body().string());
            boolean result = !json.getBoolean("error");
            Log.d(TAG, json.toString());
            return result;
        }catch(Exception ex){
            return false;
        }
    }

    public static Bitmap download(String url, ProgressBar progress){
        
        if (cl == null){
            Log.d(TAG, "client is not defined!");
            return null;
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        try(Response response = cl.newCall(request).execute()){

        }catch (Exception e){

        }
        return null;
    }
}

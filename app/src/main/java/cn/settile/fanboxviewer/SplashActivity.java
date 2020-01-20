package cn.settile.fanboxviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.Network.FanboxParser;
import cn.settile.fanboxviewer.Network.WebViewCookieHandler;
import cn.settile.fanboxviewer.Util.Constants;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static cn.settile.fanboxviewer.Network.Common.isLoggedIn;

public class SplashActivity extends AppCompatActivity implements Runnable {
    public static SharedPreferences sp;
    final String TAG = "Main";
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.loading);
        thread = new Thread(this);
        thread.start();
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        switch (request) {
            case Constants.requestCodes.LOGIN:
                recreate();
                break;
            case Constants.requestCodes.EXIT:
                finish();
                break;
            default:
                break;
        }
        super.onActivityResult(request, result, data);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void run() {
        sp = getSharedPreferences("Configs", MODE_PRIVATE);
        boolean firstRun = false;

        if (sp.getBoolean("FirstRun", true)) {
            firstRun = true;
            sp.edit().putBoolean("FirstRun", false)
                    .apply();
        }

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = (networkInfo != null && networkInfo.isConnected());

        if (!isConnected) {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("isLoggedIn", false);
            i.putExtra("NO_NETWORK", true);
            startActivity(i);
            return;
        }

        if (firstRun) {
            Log.d(TAG, "First Run");
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Constants.requestCodes.LOGIN);
            return;
        }

        CookieManager cm = CookieManager.getInstance();
        Constants.cookie = cm.getCookie(getString(R.string.index));
        if(Constants.cookie == null){
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Constants.requestCodes.LOGIN);
            return;
        }

        Cache cache = new Cache(getCacheDir(), 1024 * 1024 * 8);

        Common.client = new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieHandler())
                .cache(cache)
                .readTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
                    final Request orig = chain.request();
                    final Request withCookie = orig.newBuilder()
                            .addHeader("Cookie", Constants.cookie).build();
                    return chain.proceed(withCookie);
                })
                .build();

        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(Common.client))
//                .loggingEnabled(true)
                .build();
        Picasso.setSingletonInstance(picasso);

        Intent i = new Intent(this, MainActivity.class);
        Future<Boolean> loginFuture = isLoggedIn();
        while (!loginFuture.isDone()) {
        }
        try {
            if(!loginFuture.get()){
                Intent i1 = new Intent(this, LoginActivity.class);
                startActivityForResult(i1, Constants.requestCodes.LOGIN);
                return;
            }
            i.putExtra("isLoggedIn", true);

            Future<Object> tmp = Executors.newSingleThreadExecutor().submit(() -> {
                FanboxParser.getMessages(true);
                FanboxParser.updateIndex();
                return null;
            });
            while (!tmp.isDone()) {
            }
            tmp.get();
        } catch (Exception ex) {
            i.putExtra("isLoggedIn", false);
        }
        startActivity(i);
        finish();
    }
}

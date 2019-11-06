package cn.settile.fanboxviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.Network.FanboxParser;
import cn.settile.fanboxviewer.Network.WebViewCookieHandler;
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
                Common.client = new OkHttpClient.Builder()
                        .cookieJar(new WebViewCookieHandler())
                        .build();
                if (result == Constants.loginResultCodes.USER) {
                    Intent i = new Intent(this, MainActivity.class);
                    i.putExtra("isLoggedIn", true);
                    startActivity(i);
                } else {
                    Intent i = new Intent(this, MainActivity.class);
                    i.putExtra("isLoggedIn", false);
                    startActivity(i);
                }
                break;
            case Constants.requestCodes.EXIT:
                finish();
                break;
            default:
                break;
        }
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
            Toast.makeText(this, R.string.login_to_proceed, Toast.LENGTH_LONG)
                    .show();
            Log.d(TAG, "First Run");
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Constants.requestCodes.LOGIN);
            return;
        }
        Constants.cookie = CookieManager.getInstance().getCookie(getString(R.string.index));
        Cache cache = new Cache(getCacheDir(), 1024 * 1024 * 8);
        Common.client = new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieHandler())
                .cache(cache)
                .addInterceptor(chain -> {
                    final Request orig = chain.request();
                    final Request withCookie = orig.newBuilder()
                            .addHeader("Cookie", Constants.cookie).build();
                    return chain.proceed(withCookie);
                })
                .build();
        Intent i = new Intent(this, MainActivity.class);
        Future<Boolean> loginFuture = isLoggedIn();
        while (!loginFuture.isDone()) {
        }
        try {
            i.putExtra("isLoggedIn", loginFuture.get());
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
        startActivityForResult(i, Constants.requestCodes.EXIT);
    }
}

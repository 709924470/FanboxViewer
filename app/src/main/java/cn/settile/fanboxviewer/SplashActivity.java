package cn.settile.fanboxviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser;
import cn.settile.fanboxviewer.Util.Constants;

public class SplashActivity extends AppCompatActivity implements Runnable {
    public static SharedPreferences sp;
    final String TAG = "Main";
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setTitle(R.string.loading);

        thread = new Thread(this);
        thread.start();
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        switch (request) {
            case Constants.requestCodes.LOGIN:
                if (result == Constants.loginResultCodes.USER) {
                    sp.edit().putBoolean("LoggedIn", true).commit();
                } else {
                    sp.edit().putBoolean("LoggedIn", false).commit();
                }
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
    protected void onResume() {
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
            runOnUiThread(() -> {
                Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
            });
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("IS_LOGGED_IN", false);
            i.putExtra("NO_NETWORK", true);
            startActivity(i);
            return;
        }

        if (!(sp.contains("LoggedIn") && sp.getBoolean("LoggedIn", false))) {
            Log.d(TAG, "Hasn't logged.");
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Constants.requestCodes.LOGIN);
            return;
        }

        CookieManager cm = CookieManager.getInstance();
        Constants.Cookie = cm.getCookie(getString(R.string.index));
        if (Constants.Cookie == null) {

            sp.edit().putBoolean("LoggedIn", false).commit();
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Constants.requestCodes.LOGIN);
            return;
        }

        Common.initClient();

        if (Common.singleton == null) {
            Common.singleton = new Picasso.Builder(this)
                    .downloader(new OkHttp3Downloader(Common.client))
                    .build();
            Picasso.setSingletonInstance(Common.singleton);
        }


        Intent i = new Intent(this, MainActivity.class);


        Future<Boolean> loginFuture = Common.isLoggedIn();
        while (!loginFuture.isDone()) {
        }
        try {
            if (!loginFuture.get()) {
                Intent i1 = new Intent(this, LoginActivity.class);
                startActivityForResult(i1, Constants.requestCodes.LOGIN);
                return;
            }
            i.putExtra("IS_LOGGED_IN", true);
            sp.edit().putBoolean("LoggedIn", true).apply();
            Future<Object> tmp = Executors.newSingleThreadExecutor().submit(() -> FanboxParser.getMessages(true));
            while (!tmp.isDone()) {
            }
            tmp.get();
        } catch (Exception ex) {
            sp.edit().putBoolean("LoggedIn", false).apply();
            i.putExtra("IS_LOGGED_IN", false);
        }


        if (sp.getBoolean("LoggedIn", false)) {
            i.putExtra("IS_LOGGED_IN", true);
        }


        Uri url = getIntent().getData();
        Log.d(TAG, "Main_onCreate_run: " + url);
        while (!Objects.equals(url, null)) {                     // no extra function calls
            String username = url.getHost().split("\\.")[0];
            String path = url.getPath();                            // main=/  posts=/posts/<id>
            if (username.equals("www")) {
                if (url.getPath().contains("@")) {
                    path = url.getPath().split("@")[1];
                    Log.d(TAG, "run: " + path);
                    if (!path.contains("/")) {
                        username = new StringBuilder(path).toString(); // Copy
                        path = "/";
                    } else {
                        username = path.split("/posts/")[0];
                    }
                    Log.d(TAG, "Main_onCreate_run: " + username + "  ---  " + path);
                } else {
                    break;
                }
            }
            Log.d(TAG, "onCreate: " + path);
            if (path.equals("/")) {
                i = new Intent(this, UserDetailActivity.class);
                i.putExtra("isURL", true);
                i.putExtra("CID", username);
            } else if (path.contains("/posts/")) {
                String postId = path.split("/posts/")[1];
                i = new Intent(this, PostDetailActivity.class);
                i.putExtra("isURL", true);
                i.putExtra("CID", username);
                i.putExtra("URL", postId);
            }
            break;
        }

        startActivity(i);
        finish();


    }
}

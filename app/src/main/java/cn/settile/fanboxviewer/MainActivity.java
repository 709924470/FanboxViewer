package cn.settile.fanboxviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.Network.WebViewCookieHandler;
import okhttp3.OkHttpClient;

import static cn.settile.fanboxviewer.Network.Common.isLoggedIn;

public class MainActivity extends AppCompatActivity {
    public static SharedPreferences sp;
    final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("Configs", MODE_PRIVATE);
        boolean firstRun = false;

        if (!sp.getBoolean("FirstRun", false)){
            firstRun = true;
            sp.edit().putBoolean("FirstRun", true)
                    .apply();
        }

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected =  (networkInfo != null && networkInfo.isConnected());

        if (!isConnected){
            Intent i = new Intent(this, MainPageAct.class);
            i.putExtra("isUser", false);
            i.putExtra("NO_NETWORK", 1);
            startActivity(i);
            return;
        }

        //String cookies = sp.getString("cookie", "F");
        if(firstRun || !isLoggedIn()) {
            Toast.makeText(this, R.string.login_to_proceed, Toast.LENGTH_LONG)
                    .show();
            Log.d(TAG, "First Run");
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, Constants.requestCodes.LOGIN);
            return;
        }
        Constants.cookie = CookieManager.getInstance().getCookie(getString(R.string.index));
        Log.d(TAG, Constants.cookie);
        Common.cl = new OkHttpClient.Builder()
                .cookieJar(new WebViewCookieHandler())
                .build();
        Intent i = new Intent(this, MainPageAct.class);
        i.putExtra("isUser", isLoggedIn());
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data){
        switch (request){
            case Constants.requestCodes.LOGIN:
                Common.cl = new OkHttpClient.Builder()
                        .cookieJar(new WebViewCookieHandler())
                        .build();
                if(result == Constants.resultCodes.USER) {
                    Intent i = new Intent(this, MainPageAct.class);
                    i.putExtra("isUser", true);
                    startActivity(i);
                }else{
                    Intent i = new Intent(this, MainPageAct.class);
                    i.putExtra("isUser", false);
                    startActivity(i);
                }
                break;
            default:
                break;
        }
    }
}

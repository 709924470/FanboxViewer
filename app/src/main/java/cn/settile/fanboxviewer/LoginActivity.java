package cn.settile.fanboxviewer;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import cn.settile.fanboxviewer.Util.Constants;

public class LoginActivity extends AppCompatActivity {
    final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle(R.string.login_to_proceed);

        CookieSyncManager.createInstance(this);

        WebView wv = findViewById(R.id.login_web_view);

        if(Build.VERSION.SDK_INT > 21)
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        else
            CookieManager.getInstance().setAcceptCookie(true);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new loginClient());

        String url = "https://www.fanbox.cc/login?return_to=https%3A%2F%2Fwww.fanbox.cc%2Fcreators%2Ffind";
        wv.loadUrl(url);
    }

    @Override
    public void onBackPressed(){
        setResult(Constants.loginResultCodes.GUEST);
        super.onBackPressed();
    }

    private class loginClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView wv, String url, Bitmap favIco){
            Log.d(TAG, url);
            if(url.equals("https://www.fanbox.cc/creators/find")){
                Log.d(TAG, "Done Loading");
                Constants.Cookie = CookieManager.getInstance().getCookie(url);
                Log.d(TAG, Constants.Cookie);
                CookieSyncManager.getInstance().sync();
                setResult(Constants.loginResultCodes.USER);
                finish();
            }
        }
    }
}

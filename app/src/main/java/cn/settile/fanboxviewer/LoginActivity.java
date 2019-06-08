package cn.settile.fanboxviewer;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginActiviry extends AppCompatActivity {
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ctx = this;

        CookieSyncManager.createInstance(this);


        WebView wv = findViewById(R.id.login_webview);
        wv.setWebViewClient(new wvClient());

        if(Build.VERSION.SDK_INT > 21)
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        else
            CookieManager.getInstance().setAcceptCookie(true);

        wv.getSettings().setJavaScriptEnabled(true);

        wv.loadUrl("https://www.pixiv.net/LoginActiviry");
    }

    private class wvClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView wv, String url){
            if(url.equals("https://www.pixiv.net/")){
                Constants.cookie = CookieManager.getInstance().getCookie(url);
                CookieSyncManager.getInstance().sync();
                ctx.
            }
        }
    }
}

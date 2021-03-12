package cn.settile.fanboxviewer;

import android.annotation.SuppressLint;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.ViewModelProvider;

import cn.settile.fanboxviewer.Util.Constants;
import cn.settile.fanboxviewer.ViewModels.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    final String TAG = "Login";

    LoginViewModel viewModel = null;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setActionBar(findViewById(R.id.login_toolbar));
        setTitle(R.string.login_to_proceed);

        prepareUIAndActions();

        
        CookieSyncManager.createInstance(this);

        WebView wv = findViewById(R.id.login_web_view);

        if (Build.VERSION.SDK_INT > 21)
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        else
            CookieManager.getInstance().setAcceptCookie(true);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new loginClient());

        String url = "https://www.fanbox.cc/login?return_to=https%3A%2F%2Fwww.fanbox.cc%2Fcreators%2Ffind";
        wv.loadUrl(url);

    }

    @Override
    public void onBackPressed() {
        setResult(Constants.loginResultCodes.GUEST);
        super.onBackPressed();
    }

    private void prepareUIAndActions() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.is_loaded().observe(this, (it) -> {
            if (it) {
                ((ProgressBar) findViewById(R.id.login_progressbar)).setVisibility(View.GONE);
            }
        });
    }

    private class loginClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            viewModel.update_isloaded(true);
            super.onPageFinished(view, url);
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public void onPageStarted(WebView wv, String url, Bitmap favIco) {
            Log.d(TAG, url);
            if (url.equals("https://www.fanbox.cc/creators/find")) {
                Constants.Cookie = CookieManager.getInstance().getCookie(url);
                Log.d(TAG, Constants.Cookie);
                CookieSyncManager.getInstance().sync();
                setResult(Constants.loginResultCodes.USER);
                finish();
            }
            super.onPageStarted(wv, url, favIco);
        }
    }
}

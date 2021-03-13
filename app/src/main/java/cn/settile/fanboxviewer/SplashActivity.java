package cn.settile.fanboxviewer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Future;

import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.Util.Constants;
import cn.settile.fanboxviewer.ViewModels.SplashViewModel;

public class SplashActivity extends AppCompatActivity implements Runnable {
    final static int PERMISSION_REQUEST_CODE = 2;
    final static String TAG = "Main";
    public static SharedPreferences sp;
    SplashViewModel viewModel;
    Thread thread;
    Boolean loginOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setTitle(R.string.loading);
        prepareUIAndActions();
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            viewModel.update_storage_permission_state(Constants.CheckItemState.SUCCESS);
        } else {
            viewModel.update_storage_permission_state(Constants.CheckItemState.FAIL);
        }
        thread = new Thread(this);
        thread.start();
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        switch (request) {
            case Constants.requestCodes.LOGIN:
                //loginOnce = false;
                if (result == Constants.loginResultCodes.USER) {
                    sp.edit().putBoolean("LoggedIn", true).commit();
                } else {
                    sp.edit().putBoolean("LoggedIn", false).commit();
                    finish();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    viewModel.update_storage_permission_state(Constants.CheckItemState.SUCCESS);
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    viewModel.update_storage_permission_state(Constants.CheckItemState.FAIL);
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
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
            viewModel.update_network_state(Constants.CheckItemState.FAIL);
//            Intent i = new Intent(this, MainActivity.class);
//            i.putExtra("IS_LOGGED_IN", false);
//            i.putExtra("NO_NETWORK", true);
//            startActivity(i);
            return;
        }
        viewModel.update_network_state(Constants.CheckItemState.SUCCESS);

        if (!(sp.contains("LoggedIn") && sp.getBoolean("LoggedIn", false))) {
            Log.d(TAG, "Hasn't logged.");
            viewModel.update_cookie_state(Constants.CheckItemState.FAIL);
            startLoginActivity();
            return;
        }

        CookieManager cm = CookieManager.getInstance();
        Constants.Cookie = cm.getCookie(getString(R.string.index));
        if (Constants.Cookie == null) {
            viewModel.update_cookie_state(Constants.CheckItemState.FAIL);
            sp.edit().putBoolean("LoggedIn", false).commit();
            startLoginActivity();
            return;
        }

//        if (Common.singleton == null) {
//            Common.singleton = new Picasso.Builder(this)
//                    .downloader(new OkHttp3Downloader(Common.getClientInstance()))
//                    .build();
//            Picasso.setSingletonInstance(Common.singleton);
//        }

        Intent i = new Intent(this, MainActivity.class);


        Future<Boolean> loginFuture = Common.isLoggedIn();
        while (!loginFuture.isDone()) {
        }
        try {
            if (!loginFuture.get()) {
                startLoginActivity();
                return;
            }
//            i.putExtra("IS_LOGGED_IN", true);
            sp.edit().putBoolean("LoggedIn", true).apply();
            viewModel.update_cookie_state(Constants.CheckItemState.SUCCESS);
//            Future<Object> tmp = Executors.newSingleThreadExecutor().submit(() -> FanboxParser.getMessages(true));
//            while (!tmp.isDone()) {
//            }
//            tmp.get();
        } catch (Exception ex) {
            sp.edit().putBoolean("LoggedIn", false).apply();
//            i.putExtra("IS_LOGGED_IN", false);
            viewModel.update_cookie_state(Constants.CheckItemState.FAIL);
        }


//        if (sp.getBoolean("LoggedIn", false)) {
//            i.putExtra("IS_LOGGED_IN", false);
//        }

//
//        Uri url = getIntent().getData();
//        Log.d(TAG, "Main_onCreate_run: " + url);
//        while (!Objects.equals(url, null)) {                     // no extra function calls
//            String username = url.getHost().split("\\.")[0];
//            String path = url.getPath();                            // main=/  posts=/posts/<id>
//            if (username.equals("www")) {
//                if (url.getPath().contains("@")) {
//                    path = url.getPath().split("@")[1];
//                    Log.d(TAG, "run: " + path);
//                    if (!path.contains("/")) {
//                        username = new StringBuilder(path).toString(); // Copy
//                        path = "/";
//                    } else {
//                        username = path.split("/posts/")[0];
//                    }
//                    Log.d(TAG, "Main_onCreate_run: " + username + "  ---  " + path);
//                } else {
//                    break;
//                }
//            }
//            Log.d(TAG, "onCreate: " + path);
//            if (path.equals("/")) {
//                i = new Intent(this, UserDetailActivity.class);
//                i.putExtra("isURL", true);
//                i.putExtra("CID", username);
//            } else if (path.contains("/posts/")) {
//                String postId = path.split("/posts/")[1];
//                i = new Intent(this, PostDetailActivity.class);
//                i.putExtra("isURL", true);
//                i.putExtra("CID", username);
//                i.putExtra("URL", postId);
//            }
//            break;
//        }

        viewModel.update_cookie_state(Constants.CheckItemState.SUCCESS);
        //startActivity(i);
        //finish();


    }

    public void requestStoragePermission(View v) {

        requestPermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);

    }

    public void prepareUIAndActions() {
        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        viewModel.getStorage_permission_state().observe(this, (it) -> {
            switch (it) {
                case UNKNOW:
                    ((ImageView) findViewById(R.id.storage_permission_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_info_black_24dp));
                    ((LinearLayout) findViewById(R.id.storage_permission_check_linearLayout)).setBackgroundColor(getColor(R.color.white));
                    break;
                case SUCCESS:
                    ((ImageView) findViewById(R.id.storage_permission_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_check_circle_outline_black_24dp));
                    ((LinearLayout) findViewById(R.id.storage_permission_check_linearLayout)).setBackgroundColor(getColor(R.color.colorSuccess));
                    checkStateAndStartMainActivity();
                    break;
                case FAIL:
                    ((ImageView) findViewById(R.id.storage_permission_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_highlight_off_black_24dp));
                    ((LinearLayout) findViewById(R.id.storage_permission_check_linearLayout)).setBackgroundColor(getColor(R.color.colorError));
                    break;
            }
        });
        viewModel.getCookie_state().observe(this, (it) -> {
            switch (it) {
                case UNKNOW:
                    ((ProgressBar) findViewById(R.id.account_state_progressBar)).setVisibility(View.VISIBLE);
                    ((ImageView) findViewById(R.id.log_state_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_info_black_24dp));
                    ((LinearLayout) findViewById(R.id.log_state_check_linearLayout)).setBackgroundColor(getColor(R.color.white));
                    break;
                case SUCCESS:
                    ((ProgressBar) findViewById(R.id.account_state_progressBar)).setVisibility(View.INVISIBLE);
                    ((ImageView) findViewById(R.id.log_state_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_check_circle_outline_black_24dp));
                    ((LinearLayout) findViewById(R.id.log_state_check_linearLayout)).setBackgroundColor(getColor(R.color.colorSuccess));
                    checkStateAndStartMainActivity();
                    break;
                case FAIL:
                    ((ProgressBar) findViewById(R.id.account_state_progressBar)).setVisibility(View.INVISIBLE);
                    ((ImageView) findViewById(R.id.log_state_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_highlight_off_black_24dp));
                    ((LinearLayout) findViewById(R.id.log_state_check_linearLayout)).setBackgroundColor(getColor(R.color.colorError));
                    break;
            }
        });
        viewModel.getNetwork_state().observe(this, (it) -> {
            switch (it) {
                case UNKNOW:
                    ((ProgressBar) findViewById(R.id.network_state_progressBar)).setVisibility(View.VISIBLE);
                    ((ImageView) findViewById(R.id.network_state_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_info_black_24dp));
                    ((LinearLayout) findViewById(R.id.network_state_check_linearLayout)).setBackgroundColor(getColor(R.color.white));
                    break;
                case SUCCESS:
                    ((ProgressBar) findViewById(R.id.network_state_progressBar)).setVisibility(View.INVISIBLE);
                    ((ImageView) findViewById(R.id.network_state_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_check_circle_outline_black_24dp));
                    ((LinearLayout) findViewById(R.id.network_state_check_linearLayout)).setBackgroundColor(getColor(R.color.colorSuccess));
                    checkStateAndStartMainActivity();
                    break;
                case FAIL:
                    viewModel.update_cookie_state(Constants.CheckItemState.FAIL);
                    ((ProgressBar) findViewById(R.id.network_state_progressBar)).setVisibility(View.INVISIBLE);
                    ((ImageView) findViewById(R.id.network_state_check_imageView)).setImageDrawable(getDrawable(R.drawable.ic_highlight_off_black_24dp));
                    ((LinearLayout) findViewById(R.id.network_state_check_linearLayout)).setBackgroundColor(getColor(R.color.colorError));
                    break;
            }
        });
    }

    public void checkStateAndStartMainActivity() {
        if (viewModel.getStorage_permission_state().getValue() != Constants.CheckItemState.SUCCESS) {

        } else if (viewModel.getCookie_state().getValue() != Constants.CheckItemState.SUCCESS) {

        } else if (viewModel.getNetwork_state().getValue() != Constants.CheckItemState.SUCCESS) {

        } else {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("IS_LOGGED_IN", true);
            startActivity(i);
            finish();
        }
    }

    public void startLoginActivity() {
        if (!loginOnce) {
            Intent i1 = new Intent(this, LoginActivity.class);
            startActivityForResult(i1, Constants.requestCodes.LOGIN);
            viewModel.update_cookie_state(Constants.CheckItemState.UNKNOW);
            loginOnce = true;
        }

    }
}

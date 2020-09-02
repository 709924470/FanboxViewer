package cn.settile.fanboxviewer;

import android.app.Application;
import android.content.Context;

import com.google.android.material.snackbar.Snackbar;

public class App extends Application {
    private static App app;

    public static Application getApplication(){
        return app;
    }
    public static Context getContext(){
        return app;
    }

    @Override
    public void onCreate() {
        app = this;
        super.onCreate();
    }
}

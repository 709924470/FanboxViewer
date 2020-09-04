package cn.settile.fanboxviewer;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.os.Environment;

import static cn.settile.fanboxviewer.Network.DownloadManager.dlm;
import static cn.settile.fanboxviewer.Util.Constants.DOWNLOAD_PATH;

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
        DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        dlm.interrupt();
        super.onTerminate();
    }

    public static Notification.Builder notificationFactory(String title, String content, String CHANNEL_ID){
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(app.getBaseContext(), CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.ic_launcher_foreground);
        }else{
            builder = new Notification.Builder(app.getBaseContext())
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.ic_launcher_foreground);
        }
        return builder;
    }
}

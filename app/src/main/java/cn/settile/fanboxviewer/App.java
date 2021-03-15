package cn.settile.fanboxviewer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.File;

import static cn.settile.fanboxviewer.Util.Constants.DOWNLOAD_PATH;
import static cn.settile.fanboxviewer.Util.Constants.NOTIFICATION_SETUP;

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
        DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+ File.separatorChar+"Fanbox";
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static Notification.Builder notificationFactory(String title, String content, String CHANNEL_ID){
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setUpNotification(CHANNEL_ID);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void setUpNotification(String CHANNEL_ID){
        if (NOTIFICATION_SETUP)
            return;
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "fanbox_notify", importance);
        channel.setDescription("fanbox_notifications");
        NotificationManager notificationManager = app.getApplicationContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}

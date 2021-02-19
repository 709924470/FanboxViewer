package cn.settile.fanboxviewer.Network;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import cn.settile.fanboxviewer.App;
import cn.settile.fanboxviewer.Network.Bean.DownloadItem;
import cn.settile.fanboxviewer.R;

import static cn.settile.fanboxviewer.App.getApplication;
import static cn.settile.fanboxviewer.App.notificationFactory;
import static cn.settile.fanboxviewer.Network.Common.initClient;
import static cn.settile.fanboxviewer.Util.Constants.MAX_DOWNLOAD_THREADS;
import static cn.settile.fanboxviewer.Util.Util.galleryAddPic;

public class DownloadManager implements Runnable {
    private static final String TAG = "Download Manager";
    private static final String CHANNEL_ID = "FANBOX_DOWNLOAD";
    public static Queue<DownloadItem> url_pth = new LinkedList<>();
    public static Thread dlm;
    private static int ID = 0;
    private static int DOWNLOAD_THREAD_COUNT = 0;

    static {
        dlm = new Thread(new DownloadManager());
        dlm.start();
    }

    public static void queue(String url, String dlPath) {
        String name = dlPath.split("/")[dlPath.split("/").length - 1];
        url_pth.add(new DownloadItem(url, dlPath, name, name));
    }

    public static void queue(DownloadItem downloadItem) {
        url_pth.add(downloadItem);
    }

    @Override
    public void run() {
        while (true) {
            if (url_pth.size() == 0 || DOWNLOAD_THREAD_COUNT >= MAX_DOWNLOAD_THREADS)
                continue;
            DownloadItem item = url_pth.remove();
            new Thread(() -> {
                DOWNLOAD_THREAD_COUNT++;
                download(item);
                DOWNLOAD_THREAD_COUNT--;
            }).start();
        }
    }

    public void download(DownloadItem item) {

        Notification.Builder notification = notificationFactory(App.getContext().getString(R.string.downloading), item.displayName, CHANNEL_ID);
        NotificationManager manager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);

        final int currentID = ID++;
        final int nextID = ID++;
        notification.setProgress(100, 0, true);
        manager.notify(currentID, notification.build());

        new URLRequestor<Boolean>(item.url, (it) -> {

            File output;
            InputStream is;
            BufferedInputStream bif;
            OutputStream op;

            try {
                Log.d(TAG, item.url);
                output = new File(item.path, item.name);
                is = it.body().byteStream();
                bif = new BufferedInputStream(is);
                op = new FileOutputStream(output);

                long length = it.body().contentLength();
                byte[] buf = new byte[1024];
                int count, total = 0;
                while ((count = bif.read(buf)) != -1) {
                    op.write(buf, 0, count);
                    total += count;
                    int percentage = (int) Math.ceil(((float) total / (float) length) * 100);
//                Log.d(TAG, new StringBuilder("download: ").append(total).append(" - ").append(length).append(" - ").append(percentage).toString());
                    notification.setProgress(100, percentage, false);
                    manager.notify(currentID, notification.build());
                }
                op.flush();
                op.close();
                is.close();
                galleryAddPic(output.getPath());
                manager.cancel(currentID);
                manager.notify(nextID, notificationFactory(App.getContext().getString(R.string.download_complete), item.displayName, CHANNEL_ID).build());
            } catch (Exception e) {
                Log.e(TAG, "download: EXCEPTION", e);
                manager.cancel(currentID);
                manager.notify(nextID, notificationFactory(App.getContext().getString(R.string.download_failed), item.displayName, CHANNEL_ID).build());
            }
            return null;
        },null);
    }

//    public static void downloadThread(String url, File file, Runnable success, Runnable fail){
//        new Thread(() -> download(url, file, success, fail)).start();
//    }
//
//    public static boolean download(String url, File output, Runnable success, Runnable fail){
//        if (client == null){
//            Log.d(TAG, "client is not defined!");
//            initClient();
//            return download(url, output, success, fail);
//        }
//
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        try(Response response = client.newCall(request).execute()){
//            InputStream is = requireNonNull(response.body()).byteStream();
//
//            BufferedInputStream bif = new BufferedInputStream(is);
//            OutputStream op = new FileOutputStream(output);
//
//            byte[] buf = new byte[1024];
//            int count;
//
//            while((count = bif.read(buf)) != -1){
//                op.write(buf, 0, count);
//            }
//
//            op.flush();
//            op.close();
//            is.close();
//        }catch (Exception e){
//            Log.e(TAG, "download: EXCEPTION", e);
//            fail.run();
//            return false;
//        }
//        success.run();
//        return true;
//    }
}

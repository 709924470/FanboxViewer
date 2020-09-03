package cn.settile.fanboxviewer.Network;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import okhttp3.Request;
import okhttp3.Response;

import static java.util.Objects.requireNonNull;

public class DownloadManager implements Runnable {
    public static Queue<Pair<String, String>> url_pth = new LinkedList<>();
    public static Thread dlm;

    static {
        dlm = new Thread(new DownloadManager());
        dlm.start();
    }

    public static void queue(String url, String dlPath){
        url_pth.add(new Pair<>(url, dlPath));
    }
    @Override
    public void run() {
        while (true){
            if(url_pth.size() == 0)
                continue;
            Pair<String, String> pair = url_pth.remove();
            String url = pair.first;
            String path = pair.second;


        }
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

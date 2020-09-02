package cn.settile.fanboxviewer.Network;

import android.util.Pair;

import java.util.LinkedList;
import java.util.Queue;

public class DownloadManager implements Runnable {
    public static Queue<Pair<String, String>> url_pth = new LinkedList<>();

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
}

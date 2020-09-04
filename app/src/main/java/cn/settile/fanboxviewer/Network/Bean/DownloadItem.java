package cn.settile.fanboxviewer.Network.Bean;

import static cn.settile.fanboxviewer.Util.Constants.DOWNLOAD_PATH;

public class DownloadItem {
    public String url, path, name, displayName;
    public DownloadItem(String url, String path, String name, String displayName){
        this.url = url;
        this.path = path;
        this.name = name;
        this.displayName = displayName;
    }
    public DownloadItem(String url, String name, String displayName){
        this.url = url;
        this.path = DOWNLOAD_PATH;
        this.name = name;
        this.displayName = displayName;
    }

    public DownloadItem(String url, String name) {
        this.url = url;
        this.path = DOWNLOAD_PATH;
        this.name = name;
        this.displayName = name;
    }
}

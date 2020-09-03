package cn.settile.fanboxviewer.Network.Bean;

public class MessageItem {
    private String url;
    private String title;
    private String msg;
    private String iconUrl;
    public String extra;

    public MessageItem(String title, String msg, String url, String iconUrl) {
        this.title = title;
        this.msg = msg;
        this.url = url;
        this.iconUrl = iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}

package cn.settile.fanboxviewer.Network.Bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class CardItem {
    @Getter
    @Setter
    @ToString.Include
    private String iconUrl;
    @Getter
    @Setter
    @ToString.Include
    private String title;
    @Getter
    @Setter
    @ToString.Include
    private String createTime;
    @Getter
    @Setter
    @ToString.Include
    private String plan;
    @Getter
    @Setter
    @ToString.Include
    private String userId;
    private String pixivId;
    @Getter
    @Setter
    @ToString.Include
    private String headerUrl;
    @Getter
    @Setter
    @ToString.Include
    private String url;
    @Getter
    @Setter
    @ToString.Include
    private String desc;
    @Getter
    @Setter
    @ToString.Include
    private String creator;

    public CardItem(String iconUrl, String headerUrl, String url,
                    String title, String desc,
                    String creator, String createTime, String plan, String userId, String pixivId) {
        this.iconUrl = iconUrl;
        this.headerUrl = headerUrl;
        this.url = url;
        this.title = title;
        this.desc = desc;
        this.creator = creator;
        this.createTime = createTime;
        this.plan = plan;
        this.userId = userId;
        this.pixivId = pixivId;
    }
    public CardItem(String iconUrl, String headerUrl, String url,
                    String title, String desc,
                    String creator, String createTime, String plan, String userId) {
        this.iconUrl = iconUrl;
        this.headerUrl = headerUrl;
        this.url = url;
        this.title = title;
        this.desc = desc;
        this.creator = creator;
        this.createTime = createTime;
        this.plan = plan;
        this.userId = userId;
        this.pixivId = "";
    }
}

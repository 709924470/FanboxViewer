package cn.settile.fanboxviewer.Adapters.Bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class CardItem {
    @Getter
    @Setter
    @ToString.Include
    private String iconUrl, title, createTime, plan, headerUrl, url, desc, creator;

    public CardItem(String iconUrl, String headerUrl, String url,
                    String title, String desc,
                    String creator, String createTime, String plan) {
        this.iconUrl = iconUrl;
        this.headerUrl = headerUrl;
        this.url = url;
        this.title = title;
        this.desc = desc;
        this.creator = creator;
        this.createTime = createTime;
        this.plan = plan;
    }
}

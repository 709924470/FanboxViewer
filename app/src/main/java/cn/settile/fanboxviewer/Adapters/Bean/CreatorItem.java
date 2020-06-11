package cn.settile.fanboxviewer.Adapters.Bean;

import java.util.ArrayList;
import java.util.List;

import lombok.ToString;

@ToString
public class CreatorItem {
    public String user, coverUrl, iconUrl, desc, name;
    public List<Object> links = new ArrayList<>();
    public List<ImageBean> images = new ArrayList<>();
    public int userId;
    public Boolean following, supporting;

    public CreatorItem setUser(String user) {
        this.user = user;
		return this;
	}

    public CreatorItem setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
		return this;
	}

    public CreatorItem setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
		return this;
	}

    public CreatorItem setDesc(String desc) {
        this.desc = desc;
		return this;
	}

    public CreatorItem setLinks(List<Object> links) {
        this.links = links;
		return this;
	}

    public CreatorItem setImages(List<ImageBean> images) {
        this.images = images;
		return this;
	}

    public CreatorItem setUserId(int userId) {
        this.userId = userId;
		return this;
	}

    public CreatorItem setFollowing(Boolean following) {
        this.following = following;
        return this;
    }

    public CreatorItem setSupporting(Boolean supporting) {
        this.supporting = supporting;
        return this;
    }

    public CreatorItem setName(String name) {
        this.name = name;
        return this;
    }
}

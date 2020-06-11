package Bean;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class CreatorBean {
    public String user, coverUrl, iconUrl, desc, name;
    public List<Object> links = new ArrayList<>();
    public List<ImageBean> images = new ArrayList<>();
    public int userId;
    public Boolean following, supporting;

    public CreatorBean setUser(String user) {
        this.user = user;
		return this;
	}

    public CreatorBean setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
		return this;
	}

    public CreatorBean setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
		return this;
	}

    public CreatorBean setDesc(String desc) {
        this.desc = desc;
		return this;
	}

    public CreatorBean setLinks(List<Object> links) {
        this.links = links;
		return this;
	}

    public CreatorBean setImages(List<ImageBean> images) {
        this.images = images;
		return this;
	}

    public CreatorBean setUserId(int userId) {
        this.userId = userId;
		return this;
	}

    public CreatorBean setFollowing(Boolean following) {
        this.following = following;
        return this;
    }

    public CreatorBean setSupporting(Boolean supporting) {
        this.supporting = supporting;
        return this;
    }

    public CreatorBean setName(String name) {
        this.name = name;
        return this;
    }
}

package cn.settile.fanboxviewer.Adapters.Bean;

import android.net.Uri;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class DetailItem {
    public enum Type {
        IMAGE,
        TEXT;
    }

    @Getter
    @Setter
    public Type type;

    @Getter
    @Setter
    public String content;

    @Getter
    @Setter
    public Uri imageUri;

    public DetailItem(Type type, String content, Uri imageUri) {
        this.type = type;
        this.content = content;
        this.imageUri = imageUri;
    }

    @Override
    public String toString(){
        return type.name() + " -> " + content;
    }
}

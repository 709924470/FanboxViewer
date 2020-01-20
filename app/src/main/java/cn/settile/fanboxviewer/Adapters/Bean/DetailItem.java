package cn.settile.fanboxviewer.Adapters.Bean;

import lombok.Getter;
import lombok.Setter;

public class DetailItem {
    public enum Type {
        IMAGE,
        TEXT,
        OTHER
    }

    @Getter
    @Setter
    public Type type;

    @Getter
    @Setter
    public String content;

    @Getter
    @Setter
    public Object extra;

    public DetailItem(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public String toString(){
        return type.name() + " -> " + content;
    }
}

package cn.settile.fanboxviewer.Adapters.Bean;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class DetailItem {
    public enum Type {
        IMAGE,
        TEXT,
        VIDEO,
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
    public List<Object> extra;

    public DetailItem(Type type, String content) {
        this.type = type;
        this.content = content;
        this.extra = new ArrayList<>();
    }

    @Override
    public String toString(){
        return type.name() + " -> " + content;
    }
}

package cn.settile.fanboxviewer.Bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CardItem implements Parcelable {
    public final int id;
    public final String header, avatar, creator, url;

    public CardItem(int id, String header, String avatar, String creator, String url){
        this.id = id;
        this.header = header;
        this.avatar = avatar;
        this.creator = creator;
        this.url = url;
    }

    protected CardItem(Parcel in){
        List<String> ls = new ArrayList<>();
        in.readStringList(ls);
        this.id = in.readInt();
        this.header = ls.get(0);
        this.avatar = ls.get(1);
        this.creator = ls.get(2);
        this.url = ls.get(3);
    }

    public static final Creator<CardItem> CREATOR = new Creator<CardItem>() {
        @Override
        public CardItem createFromParcel(Parcel in) {
            return new CardItem(in);
        }

        @Override
        public CardItem[] newArray(int size) {
            return new CardItem[size];
        }
    };

    @Override
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        List<String> ls = new ArrayList<>();
        ls.add(header);
        ls.add(avatar);
        ls.add(creator);
        ls.add(url);
        dest.writeStringList(ls);
        dest.writeInt(id);
    }
}

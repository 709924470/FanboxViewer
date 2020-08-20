package cn.settile.fanboxviewer.Network.RESTfulClient;

import android.annotation.SuppressLint;
import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.settile.fanboxviewer.Adapters.Bean.CardItem;
import cn.settile.fanboxviewer.Adapters.Bean.CreatorItem;
import cn.settile.fanboxviewer.Adapters.Bean.ImageItem;
import cn.settile.fanboxviewer.Adapters.Bean.MessageItem;
import cn.settile.fanboxviewer.BuildConfig;
import cn.settile.fanboxviewer.R;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static cn.settile.fanboxviewer.App.getContext;
import static cn.settile.fanboxviewer.Util.Util.toList;

public class FanboxParser {
    public static FanboxAPI client = null;
    private final CreatorItem creator;
    private static int messagePage = 1;

    public FanboxParser(String user) throws Exception {
        if (Objects.equals(FanboxParser.client, null)){
            throw new AssertionError("Set client first");
        }

        this.creator = new CreatorItem()
                .setUser(user);

        Call<ResponseBody> creatorInfoCaller = client.getCreatorInfo(user);
        JSONObject info = getJson(creatorInfoCaller);
        info = info.getJSONObject("body");

        List<ImageItem> images = new ArrayList<>();
        for (int i = 0; i < info.getJSONArray("profileItems").length(); i++) {
            Object e = info.getJSONArray("profileItems").get(i);
            ImageItem image = new ImageItem();
            image.url = ((JSONObject) e).getString("imageUrl");
            image.thumbUrl = ((JSONObject) e).getString("thumbnailUrl");
            images.add(image);
        }

        creator
                .setName(info.getJSONObject("user").getString("name"))
                .setCoverUrl(info.getString("coverImageUrl"))
                .setDesc(info.getString("description"))
                .setLinks(toList(info.getJSONArray("profileLinks")))
                .setImages(images)
                .setIconUrl(info.getJSONObject("user").getString("iconUrl"))
                .setUserId(info.getJSONObject("user").getInt("userId"))
                .setFollowing(info.getBoolean("isFollowed"))
                .setSupporting(info.getBoolean("isSupported"));
    }

    // Creator & post functions (Local)

    // User functions (Static)

    public static List<MessageItem> getMessages(Boolean refresh) throws Exception{
        if (refresh)
            messagePage = 1;
        return getMessages();
    }

    public static List<MessageItem> getMessages() throws Exception{
        Call<ResponseBody> caller = client.getNotificationList(messagePage++);
        JSONObject json = getJson(caller);
        List<MessageItem> items = new ArrayList<>();

        JSONArray array = json.getJSONObject("body").getJSONArray("items");
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            JSONObject user = item.getJSONObject("post").getJSONObject("user");

            String msg = item.getJSONObject("post").optString("excerpt");
            msg = msg == null ? " " : msg;

            String postId = item.getJSONObject("post").getString("id");
            String iconUrl = user.getString("iconUrl");
            CachedQueue.addPostCache(Integer.parseInt(postId), new JSONObject(item.toString()));

            MessageItem mi = new MessageItem(item.getJSONObject("post").getString("title"),
                    msg,
                    postId,
                    iconUrl);
            items.add(mi);
        }

        return items;
    }

    public static List<CardItem> getAllPosts(boolean refresh, Context c) {
        return getPosts(refresh, true, c);
    }

    public static List<CardItem> getSupportingPosts(boolean refresh, Context c) {
        return getPosts(refresh, false, c);
    }

    @SuppressLint("SimpleDateFormat")
    public static List<CardItem> getPosts(boolean refresh, boolean all, Context c) {
        return null;
    }

    public static int getUnreadMessages() throws Exception{
        Call<ResponseBody> caller = client.getUnreadNotifications();

        JSONObject json = getJson(caller).getJSONObject("body");
        return json.getInt("count");
    }

    public static void setClient(FanboxAPI client){
        if (Objects.equals(FanboxParser.client, null) && !Objects.equals(client, null)){
            FanboxParser.client = client;
        }
    }

    @NotNull
    private static JSONObject getJson(Call<ResponseBody> caller) throws Exception {
        Response<ResponseBody> resp = caller.execute();

        if (BuildConfig.DEBUG && !(Objects.equals(resp.body(), null) || !resp.isSuccessful() || resp.code() != 200)) {
            throw new AssertionError("Assertion failed");
        }

        return toJSON(resp.body().string());
    }
    private static JSONObject toJSON(String json) throws Exception{
        return new JSONObject(json);
    }
}

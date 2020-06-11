package cn.settile.fanboxviewer.Network.RESTfulClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.settile.fanboxviewer.Adapters.Bean.CreatorItem;
import cn.settile.fanboxviewer.Adapters.Bean.ImageBean;
import cn.settile.fanboxviewer.Adapters.Bean.MessageItem;
import cn.settile.fanboxviewer.BuildConfig;
import retrofit2.Call;
import retrofit2.Response;

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

        Call<String> creatorInfoCaller = client.getCreatorInfo(user);
        Response<String> creatorInfoResp = creatorInfoCaller.execute();

        if (BuildConfig.DEBUG && !(Objects.equals(creatorInfoResp.body(), null) || !creatorInfoResp.isSuccessful() || creatorInfoResp.code() != 200)) {
            throw new AssertionError("Assertion failed");
        }

        JSONObject info = toJSON(creatorInfoResp.body());
        info = info.getJSONObject("body");

        List<ImageBean> images = new ArrayList<>();
        for (int i = 0; i < info.getJSONArray("profileItems").length(); i++) {
            Object e = info.getJSONArray("profileItems").get(i);
            ImageBean image = new ImageBean();
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
        Call<String> caller = client.getNotificationList(messagePage++);
        Response<String> resp = caller.execute();

        if (BuildConfig.DEBUG && !(Objects.equals(resp.body(), null) || !resp.isSuccessful() || resp.code() != 200)) {
            throw new AssertionError("Assertion failed");
        }

        JSONObject json = toJSON(resp.body());
        List<MessageItem> items = new ArrayList<>();

        JSONArray array = json.getJSONObject("body").getJSONArray("items");
        for (int i = 0; i < array.length(); i++){
            JSONObject item = (JSONObject) array.get(i);
            item = item.getJSONObject("post");

        }

        return items;
    }

    public static int getUnreadCount() throws Exception{
        Call<String> caller = client.getUnreadNotifications();
        Response<String> resp = caller.execute();

        if (BuildConfig.DEBUG && !(Objects.equals(resp.body(), null) || !resp.isSuccessful() || resp.code() != 200)) {
            throw new AssertionError("Assertion failed");
        }

        JSONObject json = toJSON(resp.body());
        return json.getInt("count");
    }

    public static void setClient(FanboxAPI client){
        if (Objects.equals(FanboxParser.client, null)){
            FanboxParser.client = client;
        }
    }

    private static JSONObject toJSON(String json) throws Exception{
        return new JSONObject(json);
    }
}

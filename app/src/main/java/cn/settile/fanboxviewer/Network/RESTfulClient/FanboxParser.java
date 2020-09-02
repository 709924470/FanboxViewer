package cn.settile.fanboxviewer.Network.RESTfulClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.settile.fanboxviewer.Adapters.Bean.CardItem;
import cn.settile.fanboxviewer.Adapters.Bean.CreatorItem;
import cn.settile.fanboxviewer.Adapters.Bean.DetailItem;
import cn.settile.fanboxviewer.Adapters.Bean.ImageItem;
import cn.settile.fanboxviewer.Adapters.Bean.MessageItem;
import cn.settile.fanboxviewer.App;
import cn.settile.fanboxviewer.BuildConfig;
import cn.settile.fanboxviewer.R;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static cn.settile.fanboxviewer.Util.Constants.MAX_HOME_LOAD_PAGE;
import static cn.settile.fanboxviewer.Util.Constants.MAX_PLAN_LOAD_PAGE;
import static cn.settile.fanboxviewer.Util.Util.toList;

@Slf4j
public class FanboxParser {
    private static final String TAG = "Parser";
    public static FanboxAPI client = null;

    private static int messagePage = 1;
    private static String homePostNext = null;
    private static String planPostNext = null;
    private final CreatorItem creator;
    private final Context c = App.getContext();
    private String postNext = null;

    public FanboxParser(String user) throws Exception {
        if (Objects.equals(FanboxParser.client, null)){
            throw new AssertionError("Set client first");
        }
        this.creator = getUserInfo(user);
    }

    // Creator & post functions (Local)
    public List<CardItem> getUserPosts() {
        try {
            List<CardItem> lci = new ArrayList<>();
            JSONArray posts;
            JSONObject tmp;
            Pattern url = Pattern.compile(
                    "&maxPublishedDatetime=(.+?)&maxId=(\\d+)&limit=10");

            Call<ResponseBody> creatorInfoCaller = Objects.equals(postNext, null) ?
                    client.getCreatorPosts(creator.user, MAX_PLAN_LOAD_PAGE) :
                    client.getNextCreatorPosts(creator.user,
                            postNext.split("=")[0],
                            postNext.split("=")[1], MAX_PLAN_LOAD_PAGE);
            tmp = APIJSONFactory(creatorInfoCaller);
            tmp = tmp.getJSONObject("body");
            posts = tmp.getJSONArray("items");
            String nextUrl = tmp.getString("nextUrl");
            // When there is no more posts
            if(nextUrl == null || nextUrl.equals("null"))
                return lci;
            Matcher matcher = url.matcher(nextUrl);
            matcher.find();
            postNext = matcher.group(1) + "=" + matcher.group(2);

            for (int i = 0; i < posts.length(); i++) {
                JSONObject json = posts.getJSONObject(i);
                lci.add(getCardItem(json, c));
            }

            return lci;
        } catch (Exception ex) {
            log.error("EXCEPTION: " , ex);
            return null;
        }
    }

    public List<DetailItem> getPostDetail(String id) throws Exception{
        List<DetailItem> items = new ArrayList<>();

        Call<ResponseBody> creatorInfoCaller = client.getPostInfo(Integer.parseInt(id));
        JSONObject body = APIJSONFactory(creatorInfoCaller).getJSONObject("body");
        if(!body.getString("restrictedFor").equals("null")){
            items.add(new DetailItem(DetailItem.Type.TEXT,
                    String.format(c.getString(R.string.plan_formatting), body.getInt("feeRequired"))));
            items.add(new DetailItem(DetailItem.Type.IMAGE, "false"));
            return items;
        }
        switch (body.getString("type")) {
            case "image":
                JSONArray images = body.getJSONObject("body").getJSONArray("images");

                for (int i = 0; i < images.length(); i++) {
                    DetailItem tmp = new DetailItem(DetailItem.Type.IMAGE,
                            images.getJSONObject(i).getString("thumbnailUrl"));
                    tmp.extra.add(images.getJSONObject(i).getString("originalUrl"));
                    items.add(tmp);
                }
                items.add(new DetailItem(DetailItem.Type.TEXT,
                        body.getJSONObject("body").getString("text")));
                break;
            case "article": {
                JSONArray blocks = body.getJSONObject("body").getJSONArray("blocks");

                for (int i = 0; i < blocks.length(); i++) {
                    JSONObject block = blocks.getJSONObject(i);
                    switch (block.getString("type")) {
                        case "p":
                            items.add(new DetailItem(DetailItem.Type.TEXT, block.getString("text")));
                            break;
                        case "image": {
                            String imageId = block.getString("imageId");
                            JSONObject img = body.getJSONObject("body")
                                    .getJSONObject("imageMap").getJSONObject(imageId);
                            DetailItem tmp = new DetailItem(DetailItem.Type.IMAGE,
                                    img.getString("thumbnailUrl"));
                            tmp.extra.add(img.getString("originalUrl"));
                            items.add(tmp);
                            break;
                        }
                        case "file": {
                            String fileId = block.getString("fileId");
                            JSONObject file = body.getJSONObject("body").getJSONObject("fileMap").getJSONObject(fileId);
                            DetailItem tmp = new DetailItem(DetailItem.Type.OTHER, file.getString("url"));
                            tmp.extra.add(file.getInt("size"));
                            items.add(tmp);
                            break;
                        }
                    }
                }
                break;
            }
            case "file": {
                items.add(new DetailItem(DetailItem.Type.TEXT, body.getJSONObject("body").getString("text")));

                JSONArray blocks = body.getJSONObject("body").getJSONArray("files");

                for (int i = 0; i < blocks.length(); i++) {
                    JSONObject innerBody = blocks.getJSONObject(i);
                    String ext = innerBody.getString("extension");
                    String file = innerBody.getString("url");

                    DetailItem tmp;
                    if (URLConnection.guessContentTypeFromName(file).contains("video")) {
                        tmp = new DetailItem(DetailItem.Type.VIDEO, file);
                    } else {
                        tmp = new DetailItem(DetailItem.Type.OTHER, file);
                    }
                    tmp.extra.add(innerBody.getString("name"));
                    tmp.extra.add(body.getJSONObject("user").getString("userId") + body.getString("id"));
                    items.add(tmp);
                    items.add(new DetailItem(DetailItem.Type.TEXT, "\n"));
                }
                break;
            }
        }
        items.add(new DetailItem(DetailItem.Type.SPACE, ""));
        items.add(new DetailItem(DetailItem.Type.SPACE, ""));
        return items;
    }

    public JSONObject getUserDetail(){
        try {
            Call<ResponseBody> creatorInfoCaller = client.getCreatorInfo(this.creator.user);
            return APIJSONFactory(creatorInfoCaller);
        }catch (Exception e){
            log.error("EXCEPTION: " , e);
            return null;
        }
    }

    // User functions (Static)
    public static List<MessageItem> getPlans() {
        try {
            List<MessageItem> result = new ArrayList<>();
            Call<ResponseBody> creatorInfoCaller = client.getSupportingPlans();
            JSONObject jsonObject = APIJSONFactory(creatorInfoCaller);
            JSONArray plans = jsonObject.getJSONArray("body");

            for (int i = 0; i < plans.length(); i++) {
                JSONObject plan = plans.getJSONObject(i);
                String title = plan.getString("title");
                String msg = plan.getString("description");

                JSONObject user = plan.getJSONObject("user");
                String userId = user.getString("userId");
                String iconUrl = user.getString("iconUrl");

                MessageItem mi;
                mi = new MessageItem(title, msg, userId, iconUrl);
                mi.extra = plan.getString("creatorId");
                result.add(mi);
            }
            return result;
        } catch (Exception e) {
            log.error("EXCEPTION: " , e);
            return null;
        }
    }

    public static List<CreatorItem> getFollowing() throws Exception{
        Call<ResponseBody> creatorInfoCaller = client.getFollowingCreators();
        JSONObject jsonObject = APIJSONFactory(creatorInfoCaller);
        JSONArray plans = jsonObject.getJSONArray("body");
        List<CreatorItem> lci = new ArrayList<>();
        for (int i = 0; i < plans.length(); i++){
            JSONObject user = plans.getJSONObject(i);
            lci.add(getUserInfo(user.getString("creatorId")));
        }
        return lci;
    }

    private static CreatorItem getUserInfo(String user) throws Exception {
        Call<ResponseBody> creatorInfoCaller = client.getCreatorInfo(user);
        Log.d(TAG, "getUserInfo() called with: user = [" + user + "]");
        JSONObject info = APIJSONFactory(creatorInfoCaller);
        info = info.getJSONObject("body");

        List<ImageItem> images = new ArrayList<>();
        for (int i = 0; i < info.getJSONArray("profileItems").length(); i++) {
            Object e = info.getJSONArray("profileItems").get(i);
            ImageItem image = new ImageItem();
            image.url = ((JSONObject) e).getString("imageUrl");
            image.thumbUrl = ((JSONObject) e).getString("thumbnailUrl");
            images.add(image);
        }

        return new CreatorItem().setUser(user)
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

    public static List<MessageItem> getMessages(Boolean refresh) throws Exception{
        if (refresh)
            messagePage = 1;
        return getMessages();
    }

    public static List<MessageItem> getNextMessages(){
        messagePage++;
        return getMessages();
    }

    public static List<MessageItem> getMessages(){
        try {
            Call<ResponseBody> caller = client.getNotificationList(messagePage);
            JSONObject json = APIJSONFactory(caller);
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
        }catch (Exception e){
            log.error("EXCEPTION: " , e);
            return null;
        }
    }

    public static List<CardItem> getAllPosts(boolean refresh, Context c) {
        return getPosts(refresh, true, c);
    }

    public static List<CardItem> getSupportingPosts(boolean refresh, Context c) {
        return getPosts(refresh, false, c);
    }

    public static List<CardItem> getPosts(boolean refresh, boolean all, Context c) {
        try {
            if (refresh) {
                homePostNext = null;
                planPostNext = null;
            }

            List<CardItem> lci = new ArrayList<>();
            JSONArray post;
            Pattern url = Pattern.compile(
                    "maxPublishedDatetime=(.+?)&maxId=(\\d+)&");
            Call<ResponseBody> caller;

            if (all) {
                caller = Objects.equals(homePostNext, null) ?
                        client.getHomePostList(MAX_HOME_LOAD_PAGE) :
                        client.geNextHomePostList(
                                homePostNext.split("=")[0],
                                homePostNext.split("=")[1], MAX_HOME_LOAD_PAGE);
            } else {
                 caller = Objects.equals(planPostNext, null) ?
                        client.getSupportingPostList(MAX_PLAN_LOAD_PAGE) :
                        client.getNextSupportingPostList(
                                planPostNext.split("=")[0],
                                planPostNext.split("=")[1], MAX_PLAN_LOAD_PAGE);
            }
            JSONObject json = APIJSONFactory(caller);
            json = json.getJSONObject("body");

            String nextUrl = json.getString("nextUrl");
            // When there is no more posts
            if(nextUrl == null || nextUrl.equals("null"))
                return lci;
            Matcher match = url.matcher(nextUrl);
            match.find();
            homePostNext = all ?
                    match.group(1) + "=" + match.group(2) : homePostNext;
            planPostNext = !all ?
                    match.group(1) + "=" + match.group(2) : planPostNext;
            post = json.optJSONArray("items");

            for (int i = 0; i < post.length(); i++) {
                JSONObject jsonElem = post.getJSONObject(i);
//                Log.d(TAG, "getPosts: " + jsonElem.toString(4));
                lci.add(getCardItem(jsonElem, c));
            }
            return lci;
        } catch (Exception ex) {
            log.error("EXCEPTION: " , ex);
            return null;
        }
    }

    public static int getUnreadMessagesCount() throws Exception{
        Call<ResponseBody> caller = client.getUnreadMessageCount();

        return APIJSONFactory(caller).getInt("body");
    }

    public static void setClient(FanboxAPI client){
        if (Objects.equals(FanboxParser.client, null)){
            FanboxParser.client = client;
        }
    }

    @NotNull
    private static JSONObject APIJSONFactory(Call<ResponseBody> caller) throws Exception {
        if (Objects.equals(client, null))
            throw new Exception("NULL Client");
        Response<ResponseBody> resp = caller.execute();
//        Log.d(TAG, "APIJSONFactory: " + caller.request());

        if (BuildConfig.DEBUG && (Objects.equals(resp.body(), null) || !resp.isSuccessful() || resp.code() != 200)) {
            throw new AssertionError("Cannot get response!");
        }

        return toJSON(Objects.requireNonNull(resp.body()).string());
    }
    private static JSONObject toJSON(String json) throws Exception{
        return new JSONObject(json);
    }

    @SuppressLint("SimpleDateFormat")
    private static CardItem getCardItem(JSONObject json, Context c) throws JSONException, ParseException {
        String title = json.getString("title");
        String desc = json.getString("excerpt");
        int fee = json.getInt("feeRequired");
        String plan = fee == 0 ? c.getString(R.string.plan_public) : " ¥" + fee + " ";

        JSONObject user = json.getJSONObject("user");
        String userName = user.getString("name");
        String userId = json.optString("creatorId");

        String iconUrl = user.getString("iconUrl");

        String date = json.getString("updatedDatetime");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
        SimpleDateFormat sdf = new SimpleDateFormat(c.getString(R.string.date_formatting));
        date = sdf.format(df.parse(date));

        String headerUrl = json.getString("coverImageUrl");
        if(headerUrl == null || headerUrl.equals("null")){
            JSONObject body = json.optJSONObject("body");
            if(body != null){
                JSONArray image = body.optJSONArray("images");
                if(image != null){
                    headerUrl = image.getJSONObject(0).getString("thumbnailUrl");
                }
            }
        }
        return new CardItem(iconUrl, headerUrl, json.getString("id"), title, desc, userName, date, plan, userId);
    }
}

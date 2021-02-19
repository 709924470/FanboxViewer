package cn.settile.fanboxviewer.Network;

import android.annotation.SuppressLint;
import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.settile.fanboxviewer.Network.Bean.CardItem;
import cn.settile.fanboxviewer.Network.Bean.DetailItem;
import cn.settile.fanboxviewer.Network.Bean.MessageItem;
import cn.settile.fanboxviewer.R;
import cn.settile.fanboxviewer.Util.Constants;
import lombok.extern.slf4j.Slf4j;

import static cn.settile.fanboxviewer.App.getContext;

@Deprecated
@Slf4j
public class FanboxParser {

    public static HashMap<String, String> userToName = new HashMap<>();
    public static HashMap<String, String> postToCover = new HashMap<>();
    public static List<MessageItem> lastMessageList = new ArrayList<>();
    public static JSONArray allPosts;
    public static JSONArray supporting;
    public static JSONArray recommended;
    public static JSONArray plans;
    private static String messageUrl = "https://fanbox.pixiv.net/api/bell.list?page=1&skipConvertUnreadNotification=0&commentOnly=0";
    private static String messageFirstPage = messageUrl;
    private static HashMap<String, String> userToIcon = new HashMap<>();
    private static JSONObject index;
    private static String allPostsNext;
    private static String supportingNext;

    @Nullable
    public static List<MessageItem> getMessages(Boolean refresh) {
        try {
            JSONObject notification = getJSON(refresh ? messageFirstPage : messageUrl);
            if (notification == null) {
                throw new Exception("Look previous error");
            }
            messageUrl = notification.getJSONObject("body").getString("nextUrl");
            JSONArray items = notification.getJSONObject("body").getJSONArray("items");
            List<MessageItem> lmi = new ArrayList<>();

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject user = item.getJSONObject("post").getJSONObject("user");

                String msg = "";
                JSONObject body = item.getJSONObject("post").optJSONObject("body");
                if (body != null) {
                    JSONArray blocks = body.optJSONArray("blocks");
                    if (blocks != null) {
                        for (int index = 0; index < blocks.length(); index++) {
                            String message = blocks.getJSONObject(index).optString("text");
                            msg = msg.concat(message == null ? " " : message);
                        }
                    }
                }

                String postId = item.getJSONObject("post").getString("id");
                String iconUrl = user.getString("iconUrl");
                String userId = user.getString("userId");

                if (userToIcon.get(userId) == null) {
                    userToIcon.put(userId, iconUrl);
                } else {
                    iconUrl = userToIcon.get(userId);
                }

                MessageItem mi = new MessageItem(item.getJSONObject("post").getString("title"),
                        msg,
                        "https://fanbox.pixiv.net/api/post.info?postId=" + postId,
                        iconUrl);
                lmi.add(mi);
            }

            lastMessageList = lmi;
            return lmi;
        } catch (Exception ex) {
            log.error("EXCEPTION: ", ex);
            return null;
        }
    }

    public static int getUnreadMessage() {
        return Objects.requireNonNull(getJSON("https://fanbox.pixiv.net/api/bell.countUnread")).optInt("count");
    }

    public static boolean updateIndex() {
        try {
            index = getJSON("https://www.pixiv.net/ajax/fanbox/index");
            if (index == null) {
                throw new Exception("Look previous error");
            }
            JSONObject temp = getJSON("https://fanbox.pixiv.net/api/post.listHome?limit=10").getJSONObject("body");

            allPosts = temp.optJSONArray("items");
            allPostsNext = temp.optString("nextUrl");

            temp = getJSON("https://fanbox.pixiv.net/api/post.listSupporting?limit=10").getJSONObject("body");
            supporting = temp.optJSONArray("items");
            supportingNext = temp.optString("nextUrl");

            recommended = getJSON("https://fanbox.pixiv.net/api/creator.listRecommended").optJSONArray("body");

            plans = getJSON("https://fanbox.pixiv.net/api/plan.listSupporting").optJSONArray("body");
        } catch (Exception ex) {
            log.error("EXCEPTION: ", ex);
            return false;
        }
        return true;
    }

    public static List<CardItem> getAllPosts(boolean refresh, Context c) {
        return getPosts(refresh, true, c);
    }

    public static List<CardItem> getSupportingPosts(boolean refresh, Context c) {
        return getPosts(refresh, false, c);
    }

    public static JSONObject getUserDetail(String userId) {
        try {
            return getJSON("https://fanbox.pixiv.net/api/creator.get?userId=" + userId);

        } catch (Exception ex) {
            log.error("ERROR: ", ex);
        }
        return null;
    }

    public static List<MessageItem> getPlans(boolean refresh) {
        try {
            if (refresh) {
                if (!updateIndex()) {
                    throw new Exception("See Last Exception");
                }
            }
            List<MessageItem> result = new ArrayList<>();
//            android.util.Log.d("getPlans", "init");

            for (int i = 0; i < plans.length(); i++) {
                JSONObject plan = plans.getJSONObject(i);
                String title = plan.getString("title");
                String msg = plan.getString("description");

                JSONObject user = plan.getJSONObject("user");
//                String userName = user.getString("name");
                String userId = user.getString("userId");

                String iconUrl = user.getString("iconUrl");
                if (userToIcon.get(userId) == null) {
                    userToIcon.put(userId, iconUrl);
                } else {
                    iconUrl = userToIcon.get(userId);
                }

//                String url = "https://www.pixiv.net/fanbox/creator/" + userId;

                MessageItem mi;
                mi = new MessageItem(title, msg, userId, iconUrl);
                result.add(mi);
            }

//            android.util.Log.d("getPlans", "Returning ..." + result.size());

            return result;
        } catch (Exception ex) {
            log.error("EXCEPTION: ", ex);
            return null;
        }
    }

    public static String getUrl(String userId) {
        return "https://fanbox.pixiv.net/api/post.listCreator?userId=" + userId + "&limit=10";
    }

    @SuppressLint("SimpleDateFormat")
    public static List<CardItem> getPosts(boolean refresh, boolean all, Context c) {
        try {
            if (refresh) {
                if (!updateIndex()) {
                    throw new Exception("Look previous error");
                }
            }

            List<CardItem> lci = new ArrayList<>();

            JSONArray post;
            JSONObject tmp;

            if (all) {
                if (Objects.equals(allPosts, null)) {
                    getPosts(true, true, getContext());
                }
                post = new JSONArray(allPosts.toString());
                tmp = getJSON(allPostsNext);
                tmp = tmp.getJSONObject("body");
                allPosts = tmp.getJSONArray("items");
                allPostsNext = tmp.getString("nextUrl");

            } else {
                if (Objects.equals(supporting, null)) {
                    getPosts(true, true, getContext());
                }
                post = new JSONArray(supporting.toString());
                tmp = getJSON(supportingNext);
                tmp = tmp.getJSONObject("body");
                supporting = tmp.getJSONArray("items");
                supportingNext = tmp.getString("nextUrl");
            }

            for (int i = 0; i < post.length(); i++) {
                JSONObject json = post.getJSONObject(i);
                String title = json.getString("title");
                String desc = json.getString("excerpt");
                int fee = json.getInt("feeRequired");
                String plan = fee == 0 ? c.getString(R.string.plan_public) : " ¥" + fee + " ";

                JSONObject user = json.getJSONObject("user");
                String userName = user.getString("name");
                String userId = user.getString("userId");

                String iconUrl = user.getString("iconUrl");
                if (userToIcon.get(userId) == null) {
                    userToIcon.put(userId, iconUrl);
                } else {
                    iconUrl = userToIcon.get(userId);
                }

                if (userToName.get(userId) == null) {
                    userToName.put(userId, userName);
                }

                String date = json.getString("updatedDatetime");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
                SimpleDateFormat sdf = new SimpleDateFormat(c.getString(R.string.date_formatting));
                date = sdf.format(df.parse(date));

                String headerUrl = json.getString("coverImageUrl");
                if (headerUrl.equals("null") || headerUrl == null) {
                    JSONObject body = json.optJSONObject("body");
                    if (body != null) {
                        JSONArray image = body.optJSONArray("images");
                        if (image != null) {
                            headerUrl = image.getJSONObject(0).getString("thumbnailUrl");
                        }
                    }
                }

                String url = "https://fanbox.pixiv.net/api/post.info?postId=" + json.getString("id");

                if (!postToCover.containsKey(json.getString("id"))) {
                    postToCover.put(json.getString("id"), headerUrl);
                }

                //lci.add(new CardItem(iconUrl, headerUrl, url, title, desc, userName, date, plan, userId));
            }

//            android.util.Log.d("getPosts", "Returning ...");

            return lci;
        } catch (Exception ex) {
            log.error("EXCEPTION: ", ex);
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static HashMap<Integer, Object> getUserPosts(@NotNull String userId, String useUrl, @NotNull Context c) {
        try {
//            android.util.Log.d("getPosts", (refresh ? "t" : "f") + (all ? "t" : "f"));
            List<CardItem> lci = new ArrayList<>();

            JSONArray posts;
            JSONObject tmp;

            String nextUrl;

            if (Objects.equals(useUrl, null)) {
                tmp = getJSON(getUrl(userId));
                tmp = tmp.getJSONObject("body");
//                tmp = tmp.getJSONObject("post");
                posts = tmp.getJSONArray("items");
                nextUrl = tmp.getString("nextUrl");
            } else {
                String refer = "https://www.pixiv.net/fanbox/creator/" + userId + "/post";
                tmp = getJSON(useUrl, refer);
                tmp = tmp.getJSONObject("body");
                posts = tmp.getJSONArray("items");
                nextUrl = tmp.getString("nextUrl");
            }

            for (int i = 0; i < posts.length(); i++) {
                JSONObject json = posts.getJSONObject(i);
//                android.util.Log.d("getPosts", i + " -> " + json.toString());
                String title = json.getString("title");
                String desc = json.getString("excerpt");
                int fee = json.getInt("feeRequired");
                String plan = fee == 0 ? c.getString(R.string.plan_public) : " ¥" + fee + " ";

                JSONObject user = json.getJSONObject("user");
                String userName = user.getString("name");
                String pixivId = user.getString("userId");

                String iconUrl = user.getString("iconUrl");
                if (userToIcon.get(userId) == null) {
                    userToIcon.put(userId, iconUrl);
                } else {
                    iconUrl = userToIcon.get(userId);
                }

                if (userToName.get(userId) == null) {
                    userToName.put(userId, userName);
                }

                String date = json.getString("updatedDatetime");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
                SimpleDateFormat sdf = new SimpleDateFormat(c.getString(R.string.date_formatting));
                date = sdf.format(df.parse(date));

                String headerUrl = json.getString("coverImageUrl");
                if (headerUrl == null || headerUrl.equals("null")) {
                    JSONObject body = json.optJSONObject("body");
                    if (body != null) {
                        JSONArray image = body.optJSONArray("images");
                        if (image != null) {
                            headerUrl = image.getJSONObject(0).getString("thumbnailUrl");
                        }
                    }
                }

                String url = "https://fanbox.pixiv.net/api/post.info?postId=" + json.getString("id");

                if (!postToCover.containsKey(json.getString("id"))) {
                    postToCover.put(json.getString("id"), headerUrl);
                }

                lci.add(new CardItem(iconUrl, headerUrl, url, title, desc, userName, date, plan, userId, pixivId));
            }

            HashMap<Integer, Object> result = new HashMap<>();
            result.put(0, nextUrl);
            result.put(1, lci);

            return result;
        } catch (Exception ex) {
            log.error("EXCEPTION: ", ex);
            return null;
        }
    }

    public static List<DetailItem> getPostDetail(String url, Context c) throws Exception {
        List<DetailItem> items = new ArrayList<>();

        JSONObject req = getJSON(url);

        JSONObject body = req.getJSONObject("body");
        if (!body.getString("restrictedFor").equals("null")) {
            items.add(new DetailItem(DetailItem.Type.TEXT, String.format(c.getString(R.string.plan_formatting), body.getInt("feeRequired"))));
            items.add(new DetailItem(DetailItem.Type.IMAGE, "false"));
            return items;
        }
        if (body.getString("type").equals("image")) {
            JSONArray images = body.getJSONObject("body").getJSONArray("images");

            for (int i = 0; i < images.length(); i++) {
                DetailItem tmp = new DetailItem(DetailItem.Type.IMAGE, images.getJSONObject(i).getString("thumbnailUrl"));
                tmp.extra.add(images.getJSONObject(i).getString("originalUrl"));
                items.add(tmp);
            }
            items.add(new DetailItem(DetailItem.Type.TEXT, body.getJSONObject("body").getString("text")));
        } else if (body.getString("type").equals("article")) {
            JSONArray blocks = body.getJSONObject("body").getJSONArray("blocks");

            for (int i = 0; i < blocks.length(); i++) {
                JSONObject block = blocks.getJSONObject(i);
                if (block.getString("type").equals("p")) {
                    items.add(new DetailItem(DetailItem.Type.TEXT, block.getString("text")));
                } else if (block.getString("type").equals("image")) {
                    String imageId = block.getString("imageId");
                    JSONObject img = body.getJSONObject("body").getJSONObject("imageMap").getJSONObject(imageId);

                    DetailItem tmp = new DetailItem(DetailItem.Type.IMAGE, img.getString("thumbnailUrl"));
                    tmp.extra.add(img.getString("originalUrl"));
                    items.add(tmp);
                } else if (block.getString("type").equals("file")) {
                    String fileId = block.getString("fileId");

                    JSONObject file = body.getJSONObject("body").getJSONObject("fileMap").getJSONObject(fileId);
                    DetailItem tmp = new DetailItem(DetailItem.Type.OTHER, file.getString("url"));
                    tmp.extra.add(file.getInt("size"));
                    items.add(tmp);
                }
            }
        } else if (body.getString("type").equals("file")) {
            items.add(new DetailItem(DetailItem.Type.TEXT, body.getJSONObject("body").getString("text")));

            JSONArray blocks = body.getJSONObject("body").getJSONArray("files");

            for (int i = 0; i < blocks.length(); i++) {
                JSONObject innerBody = blocks.getJSONObject(i);
                String ext = innerBody.getString("extension");
                String file = innerBody.getString("url");
//                int size = innerBody.getInt("size");

                DetailItem tmp;
                if (URLConnection.guessContentTypeFromName(file).contains("video")) {
                    tmp = new DetailItem(DetailItem.Type.VIDEO, file);
                } else {
                    tmp = new DetailItem(DetailItem.Type.OTHER, file);
                }
                tmp.extra.add(innerBody.getString("name"));
                tmp.extra.add(concatUrl(body.getJSONObject("user").getString("userId"), body.getString("id")));
                items.add(tmp);
                items.add(new DetailItem(DetailItem.Type.TEXT, "\n"));
            }
        }
        return items;
    }

    @Nullable
    private static JSONObject getJSON(String url, String refer) {
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Refer", refer);
        headers.put("Origin", "https://www.pixiv.net");
        URLRequestor<JSONObject> ur=new URLRequestor<JSONObject>(url, (it) -> {
            try {
                Constants.Cookie = it.header("Set-Cookie", Constants.Cookie);
                String response = it.body().string();
                return new JSONObject(response);
            } catch (Exception ex) {
                log.error("EXCEPTION: ", ex);
                return null;
            }
        }, headers);
        return ur.getReturnValue();
    }

    @Nullable
    public static JSONObject getJSON(String url) {
        return getJSON(url, "https://www.pixiv.net/fanbox/");
    }

    @Nullable
    public static String concatUrl(String userid, String postid) {
        return "https://www.pixiv.net/fanbox/creator/" + userid + "/post/" + postid;
    }
}

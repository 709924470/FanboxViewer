package cn.settile.fanboxviewer.Network;

import android.annotation.SuppressLint;
import android.content.Context;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.settile.fanboxviewer.Adapters.Bean.CardItem;
import cn.settile.fanboxviewer.Adapters.Bean.MessageItem;
import cn.settile.fanboxviewer.Constants;
import cn.settile.fanboxviewer.R;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class FanboxParser {

    private static String messageUrl = "https://www.pixiv.net/ajax/fanbox/hhw/notification?page=1&skipConvertUnreadNotification=0&commentOnly=0";
    private static String messageFirstPage = messageUrl;
    private static HashMap<String, String> userToIcon = new HashMap<>();

    public static List<MessageItem> lastMessageList = new ArrayList<>();


    private static JSONObject index;

    public static JSONArray allPosts;
    private static String allPostsNext;

    public static JSONArray supporting;
    private static String supportingNext;

    public static JSONArray recommended;
    public static JSONArray plans;

    @Nullable
    public static List<MessageItem> getMessages(Boolean refresh) {
        try {
            JSONObject notification = getJSON(refresh ? messageFirstPage : messageUrl);
            messageUrl = "https://www.pixiv.net" + notification.getJSONObject("body").getString("nextUrl");
            if (notification == null) {
                throw new Exception("Look previous error");
            }
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

                String userId = item.getJSONObject("post").getString("id");
                String iconUrl = user.getString("iconUrl");

                if (userToIcon.get(userId) == null) {
                    userToIcon.put(userId, iconUrl);
                } else {
                    iconUrl = userToIcon.get(userId);
                }

                MessageItem mi = new MessageItem(item.getJSONObject("post").getString("title"),
                        msg,
                        "https://www.pixiv.net/fanbox/creator/" +
                                user.getString("userId") + "/post/" +
                                userId,
                        iconUrl);
                lmi.add(mi);
            }

            lastMessageList = lmi;
            return lmi;
        } catch (Exception ex) {
            log.error("EXCEPTION: " , ex);
            return null;
        }
    }

    public static boolean updateIndex() {
        try {
            index = getJSON("https://www.pixiv.net/ajax/fanbox/index");
            if (index == null) {
                throw new Exception("Look previous error");
            }
            JSONObject body = index.optJSONObject("body");
            JSONObject temp = body.optJSONObject("postListForHome");

            allPosts = temp.optJSONArray("items");
            allPostsNext = temp.optString("nextUrl");

            temp = body.optJSONObject("postListOfSupporting");
            supporting = temp.optJSONArray("items");
            supportingNext = temp.optString("nextUrl");

            recommended = body.optJSONArray("recommendedCreators");

            plans = body.optJSONArray("supportingPlans");
        } catch (Exception ex) {
            log.error("EXCEPTION: " , ex);
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

    public static List<MessageItem> getPlans(boolean refresh) {
        try {
            if(refresh){
                if (!updateIndex()){
                    throw new Exception("See Last Exception");
                }
            }
            List<MessageItem> result = new ArrayList<>();

            for(int i = 0; i < plans.length(); i++){
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

                String url = "https://www.pixiv.net/fanbox/creator/" + userId;

                MessageItem mi;
                mi = new MessageItem(title, msg, url, iconUrl);
                result.add(mi);
            }

            return result;
        } catch (Exception ex) {
            log.error("EXCEPTION: " , ex);
            return null;
        }
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
                post = new JSONArray(allPosts.toString());
                tmp = getJSON(allPostsNext);
                tmp = tmp.getJSONObject("body");
                allPosts = tmp.getJSONArray("items");
                allPostsNext = tmp.getString("nextUrl");

            } else {
                post = new JSONArray(supporting.toString());
                tmp = getJSON(supportingNext);
                tmp = tmp.getJSONObject("body");
                supporting = tmp.getJSONArray("items");
                supportingNext = tmp.getString("nextUrl");
            }

            tmp = null;


            for (int i = 0; i < post.length(); i++) {
                JSONObject json = post.getJSONObject(i);
                log.debug(i + " -> " + json.toString());
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

                String date = json.getString("updatedDatetime");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
                SimpleDateFormat sdf = new SimpleDateFormat(c.getString(R.string.date_formatting));
                date = sdf.format(df.parse(date));

                String headerUrl = json.getString("coverImageUrl");
                if(headerUrl.equals("null") || headerUrl == null){
                    JSONObject body = json.optJSONObject("body");
                    if(body != null){
                        JSONArray image = body.optJSONArray("images");
                        if(image != null){
                            headerUrl = image.getJSONObject(0).getString("thumbnailUrl");
                        }
                    }
                }

                String url = "https://www.pixiv.net/fanbox/creator/" + userId + "/post/" + json.getString("id");

                lci.add(new CardItem(iconUrl, headerUrl, url, title, desc, userName, date, plan));
            }

            return lci;
        } catch (Exception ex) {
            log.error("EXCEPTION: " , ex);
            return null;
        }
    }

    private static JSONObject getJSON(String url) {
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Refer", "https://www.pixiv.net/fanbox/")
                .addHeader("Origin", "https://www.pixiv.net")
                .build();
        try (Response resp = Common.client.newCall(req).execute()) {
            Constants.cookie = resp.header("Set-Cookie", Constants.cookie);
            String response = resp.body().string();
            return new JSONObject(response);
        } catch (Exception ex) {
            log.error("EXCEPTION: " , ex);
            return null;
        }
    }
}

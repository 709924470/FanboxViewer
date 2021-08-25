package cn.settile.fanboxviewer.Network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import cn.settile.fanboxviewer.App
import cn.settile.fanboxviewer.Network.Bean.CardItem
import cn.settile.fanboxviewer.Network.Bean.DetailItem
import cn.settile.fanboxviewer.Network.Bean.MessageItem
import cn.settile.fanboxviewer.Network.URLRequestor.OnResponseListener
import cn.settile.fanboxviewer.R
import cn.settile.fanboxviewer.Util.Constants
import lombok.extern.slf4j.Slf4j
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLConnection
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Deprecated("")
@Slf4j
object FanboxParser {
    private const val TAG = "FanboxParser"
    public var userToName = HashMap<String, String?>()
    var postToCover = HashMap<String, String?>()
    var lastMessageList: List<MessageItem> = ArrayList()
    var allPosts: JSONArray? = null
    var supporting: JSONArray? = null
    var recommended: JSONArray? = null
    var plans: JSONArray? = null
    private var messageUrl =
        "https://api.fanbox.cc/bell.list?page=1&skipConvertUnreadNotification=0&commentOnly=0"
    private val messageFirstPage = messageUrl
    private val userToIcon = HashMap<String, String?>()
    private var index: JSONObject? = null
    private var allPostsNext: String? = null
    private var supportingNext: String? = null
    fun getMessages(refresh: Boolean): List<MessageItem>? {
        return try {
            val notification = getJSON(if (refresh) messageFirstPage else messageUrl)
                ?: throw Exception("Look previous error")
            messageUrl = notification.getJSONObject("body").getString("nextUrl")
            val items = notification.getJSONObject("body").getJSONArray("items")
            val lmi: MutableList<MessageItem> = ArrayList()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val user = item.getJSONObject("post").getJSONObject("user")
                var msg = ""
                val body = item.getJSONObject("post").optJSONObject("body")
                if (body != null) {
                    val blocks = body.optJSONArray("blocks")
                    if (blocks != null) {
                        for (index in 0 until blocks.length()) {
                            val message = blocks.getJSONObject(index).optString("text")
                            msg = msg + (message ?: " ")
                        }
                    }
                }
                val postId = item.getJSONObject("post").getString("id")
                var iconUrl = user.getString("iconUrl")
                val userId = user.getString("userId")
                if (userToIcon[userId] == null) {
                    userToIcon[userId] = iconUrl
                } else {
                    iconUrl = userToIcon[userId]
                }
                val mi = MessageItem(
                    item.getJSONObject("post").getString("title"),
                    msg,
                    "https://fanbox.pixiv.net/api/post.info?postId=$postId",
                    iconUrl
                )
                lmi.add(mi)
            }
            lastMessageList = lmi
            return lmi
        } catch (ex: Exception) {
            Log.e(TAG, "EXCEPTION: " + ex)
            return null
        }
    }

    val unreadMessage: Int?
        get() = getJSON("https://fanbox.pixiv.net/api/bell.countUnread")
            ?.optInt("count", 0)

    fun updateIndex(): Boolean {
        try {
            index = getJSON("https://www.pixiv.net/ajax/fanbox/index")
            if (index == null) {
                throw Exception("Look previous error")
            }
            var temp = getJSON("https://fanbox.pixiv.net/api/post.listHome?limit=10")!!
                .getJSONObject("body")
            allPosts = temp.optJSONArray("items")
            allPostsNext = temp.optString("nextUrl")
            temp = getJSON("https://fanbox.pixiv.net/api/post.listSupporting?limit=10")!!
                .getJSONObject("body")
            supporting = temp.optJSONArray("items")
            supportingNext = temp.optString("nextUrl")
            recommended = getJSON("https://fanbox.pixiv.net/api/creator.listRecommended")!!
                .optJSONArray("body")
            plans = getJSON("https://fanbox.pixiv.net/api/plan.listSupporting")!!
                .optJSONArray("body")
        } catch (ex: Exception) {
            Log.e(TAG, "EXCEPTION: " + ex)
            return false
        }
        return true
    }

    fun getAllPosts(refresh: Boolean, c: Context): List<CardItem>? {
        return getPosts(refresh, true, c)
    }

    fun getSupportingPosts(refresh: Boolean, c: Context): List<CardItem>? {
        return getPosts(refresh, false, c)
    }

    fun getUserDetail(userId: String): JSONObject? {
        try {
            return getJSON("https://fanbox.pixiv.net/api/creator.get?userId=$userId")
        } catch (ex: Exception) {
            Log.e(TAG, "EXCEPTION: " + ex)
        }
        return null
    }

    fun getPlans(refresh: Boolean): List<MessageItem>? {
        return try {
            if (refresh) {
                if (!updateIndex()) {
                    throw Exception("See Last Exception")
                }
            }
            val result: MutableList<MessageItem> = ArrayList()
            //            android.util.Log.d("getPlans", "init");
            for (i in 0 until plans!!.length()) {
                val plan = plans!!.getJSONObject(i)
                val title = plan.getString("title")
                val msg = plan.getString("description")
                val user = plan.getJSONObject("user")
                //                String userName = user.getString("name");
                val userId = user.getString("userId")
                var iconUrl = user.getString("iconUrl")
                if (userToIcon[userId] == null) {
                    userToIcon[userId] = iconUrl
                } else {
                    iconUrl = userToIcon[userId]
                }

                //                String url = "https://www.pixiv.net/fanbox/creator/" + userId;
                var mi: MessageItem
                mi = MessageItem(title, msg, userId, iconUrl)
                result.add(mi)
            }

            //            android.util.Log.d("getPlans", "Returning ..." + result.size());
            return result
        } catch (ex: Exception) {
            Log.e(TAG, "EXCEPTION: " + ex)
            return null
        }
    }

    fun getUrl(userId: String): String {
        return "https://fanbox.pixiv.net/api/post.listCreator?userId=$userId&limit=10"
    }

    @SuppressLint("SimpleDateFormat")
    fun getPosts(refresh: Boolean, all: Boolean, c: Context): List<CardItem>? {
        return try {
            if (refresh) {
                if (!updateIndex()) {
                    throw Exception("Look previous error")
                }
            }
            val lci: List<CardItem> = ArrayList()
            val post: JSONArray
            var tmp: JSONObject?
            if (all) {
                if (allPosts == null) {
                    getPosts(true, true, App.getContext())
                }
                post = JSONArray(allPosts.toString())
                tmp = getJSON(allPostsNext)
                tmp = tmp!!.getJSONObject("body")
                allPosts = tmp.getJSONArray("items")
                allPostsNext = tmp.getString("nextUrl")
            } else {
                if (supporting == null) {
                    getPosts(true, true, App.getContext())
                }
                post = JSONArray(supporting.toString())
                tmp = getJSON(supportingNext)
                tmp = tmp!!.getJSONObject("body")
                supporting = tmp.getJSONArray("items")
                supportingNext = tmp.getString("nextUrl")
            }
            for (i in 0 until post.length()) {
                val json = post.getJSONObject(i)
                val title = json.getString("title")
                val desc = json.getString("excerpt")
                val fee = json.getInt("feeRequired")
                val plan = if (fee == 0) c.getString(R.string.plan_public) else " ¥$fee "
                val user = json.getJSONObject("user")
                val userName = user.getString("name")
                val userId = user.getString("userId")
                var iconUrl = user.getString("iconUrl")
                if (userToIcon[userId] == null) {
                    userToIcon[userId] = iconUrl
                } else {
                    iconUrl = userToIcon[userId]
                }
                if (userToName[userId] == null) {
                    userToName[userId] = userName
                }
                var date = json.getString("updatedDatetime")
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ")
                val sdf = SimpleDateFormat(c.getString(R.string.date_formatting))
                date = sdf.format(df.parse(date))
                var headerUrl = json.getString("coverImageUrl")
                if (headerUrl == "null" || headerUrl == null) {
                    val body = json.optJSONObject("body")
                    if (body != null) {
                        val image = body.optJSONArray("images")
                        if (image != null) {
                            headerUrl = image.getJSONObject(0).getString("thumbnailUrl")
                        }
                    }
                }
                val url = "https://fanbox.pixiv.net/api/post.info?postId=" + json.getString("id")
                if (!postToCover.containsKey(json.getString("id"))) {
                    postToCover[json.getString("id")] =
                        headerUrl
                }

                //lci.add(new CardItem(iconUrl, headerUrl, url, title, desc, userName, date, plan, userId));
            }

            //            android.util.Log.d("getPosts", "Returning ...");
            return lci
        } catch (ex: Exception) {
            Log.e(TAG, "EXCEPTION: " + ex)
            return null
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getUserPosts(userId: String, useUrl: String?, c: Context): HashMap<Int, Any>? {
        return try {
            //            android.util.Log.d("getPosts", (refresh ? "t" : "f") + (all ? "t" : "f"));
            val lci: MutableList<CardItem> = ArrayList()
            val posts: JSONArray
            var tmp: JSONObject?
            val nextUrl: String
            if (useUrl == null) {
                tmp = getJSON(getUrl(userId))
                tmp = tmp!!.getJSONObject("body")
                //                tmp = tmp.getJSONObject("post");
                posts = tmp.getJSONArray("items")
                nextUrl = tmp.getString("nextUrl")
            } else {
                val refer = "https://www.pixiv.net/fanbox/creator/$userId/post"
                tmp = getJSON(useUrl, refer)
                tmp = tmp!!.getJSONObject("body")
                posts = tmp.getJSONArray("items")
                nextUrl = tmp.getString("nextUrl")
            }
            for (i in 0 until posts.length()) {
                val json = posts.getJSONObject(i)
                //                android.util.Log.d("getPosts", i + " -> " + json.toString());
                val title = json.getString("title")
                val desc = json.getString("excerpt")
                val fee = json.getInt("feeRequired")
                val plan = if (fee == 0) c.getString(R.string.plan_public) else " ¥$fee "
                val user = json.getJSONObject("user")
                val userName = user.getString("name")
                val pixivId = user.getString("userId")
                var iconUrl = user.getString("iconUrl")
                if (userToIcon[userId] == null) {
                    userToIcon[userId] = iconUrl
                } else {
                    iconUrl = userToIcon[userId]
                }
                if (userToName[userId] == null) {
                    userToName[userId] = userName
                }
                var date = json.getString("updatedDatetime")
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ")
                val sdf = SimpleDateFormat(c.getString(R.string.date_formatting))
                date = sdf.format(df.parse(date))
                var headerUrl = json.getString("coverImageUrl")
                if (headerUrl == null || headerUrl == "null") {
                    val body = json.optJSONObject("body")
                    if (body != null) {
                        val image = body.optJSONArray("images")
                        if (image != null) {
                            headerUrl = image.getJSONObject(0).getString("thumbnailUrl")
                        }
                    }
                }
                val url = "https://fanbox.pixiv.net/api/post.info?postId=" + json.getString("id")
                if (!postToCover.containsKey(json.getString("id"))) {
                    postToCover[json.getString("id")] =
                        headerUrl
                }
                lci.add(
                    CardItem(
                        iconUrl,
                        headerUrl,
                        url,
                        title,
                        desc,
                        userName,
                        date,
                        plan,
                        userId,
                        pixivId
                    )
                )
            }
            val result = HashMap<Int, Any>()
            result[0] = nextUrl
            result[1] = lci
            return result
        } catch (ex: Exception) {
            Log.e(TAG, "EXCEPTION: " + ex)
            return null
        }
    }

    @Throws(Exception::class)
    fun getPostDetail(url: String?, c: Context): List<DetailItem> {
        val items: MutableList<DetailItem> = ArrayList()
        val req = getJSON(url)
        val body = req!!.getJSONObject("body")
        if (body.getString("restrictedFor") != "null") {
            items.add(
                DetailItem(
                    DetailItem.Type.TEXT,
                    String.format(c.getString(R.string.plan_formatting), body.getInt("feeRequired"))
                )
            )
            items.add(DetailItem(DetailItem.Type.IMAGE, "false"))
            return items
        }
        if (body.getString("type") == "image") {
            val images = body.getJSONObject("body").getJSONArray("images")
            for (i in 0 until images.length()) {
                val tmp = DetailItem(
                    DetailItem.Type.IMAGE,
                    images.getJSONObject(i).getString("thumbnailUrl")
                )
                tmp.extra.add(images.getJSONObject(i).getString("originalUrl"))
                items.add(tmp)
            }
            items.add(
                DetailItem(
                    DetailItem.Type.TEXT,
                    body.getJSONObject("body").getString("text")
                )
            )
        } else if (body.getString("type") == "article") {
            val blocks = body.getJSONObject("body").getJSONArray("blocks")
            for (i in 0 until blocks.length()) {
                val block = blocks.getJSONObject(i)
                if (block.getString("type") == "p") {
                    items.add(DetailItem(DetailItem.Type.TEXT, block.getString("text")))
                } else if (block.getString("type") == "image") {
                    val imageId = block.getString("imageId")
                    val img =
                        body.getJSONObject("body").getJSONObject("imageMap").getJSONObject(imageId)
                    val tmp = DetailItem(DetailItem.Type.IMAGE, img.getString("thumbnailUrl"))
                    tmp.extra.add(img.getString("originalUrl"))
                    items.add(tmp)
                } else if (block.getString("type") == "file") {
                    val fileId = block.getString("fileId")
                    val file =
                        body.getJSONObject("body").getJSONObject("fileMap").getJSONObject(fileId)
                    val tmp = DetailItem(DetailItem.Type.OTHER, file.getString("url"))
                    tmp.extra.add(file.getInt("size"))
                    items.add(tmp)
                }
            }
        } else if (body.getString("type") == "file") {
            items.add(
                DetailItem(
                    DetailItem.Type.TEXT,
                    body.getJSONObject("body").getString("text")
                )
            )
            val blocks = body.getJSONObject("body").getJSONArray("files")
            for (i in 0 until blocks.length()) {
                val innerBody = blocks.getJSONObject(i)
                val ext = innerBody.getString("extension")
                val file = innerBody.getString("url")
                //                int size = innerBody.getInt("size");
                var tmp: DetailItem
                tmp = if (URLConnection.guessContentTypeFromName(file).contains("video")) {
                    DetailItem(DetailItem.Type.VIDEO, file)
                } else {
                    DetailItem(DetailItem.Type.OTHER, file)
                }
                tmp.extra.add(innerBody.getString("name"))
                tmp.extra.add(
                    concatUrl(
                        body.getJSONObject("user").getString("userId"),
                        body.getString("id")
                    )
                )
                items.add(tmp)
                items.add(DetailItem(DetailItem.Type.TEXT, "\n"))
            }
        }
        return items
    }


    //TODO Repair User Posts. Change To Async.
    private fun getJSON(url: String?, refer: String): JSONObject? {
        val headers = HashMap<String, String>()
        headers["Refer"] = refer
        headers["Origin"] = "https://www.pixiv.net"
        val ur = URLRequestor<JSONObject?>(url!!, OnResponseListener {
            Log.d("[Sync SUCCEEDED]", "getJSON");
            var obj: JSONObject?
            try {
                Constants.Cookie =
                    it.header("Set-Cookie", Constants.Cookie)
                val response = it.body!!.string()
                obj = JSONObject(response)
            } catch (ex: Exception) {
                Log.e(TAG, "EXCEPTION: " + ex)
                obj = null
            }
            return@OnResponseListener obj;
        }, headers).sync()
        return ur.getReturnValue()
    }

    fun getJSON(url: String?): JSONObject? {
        return getJSON(url, "https://www.pixiv.net/fanbox/")
    }

    fun concatUrl(userid: String, postid: String): String? {
        return "https://www.pixiv.net/fanbox/creator/$userid/post/$postid"
    }
}
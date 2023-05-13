package cn.settile.fanboxviewer.Network

import android.util.Log
import cn.settile.fanboxviewer.Network.Bean.SupportingItem
import cn.settile.fanboxviewer.Util.Constants
import org.json.JSONException
import org.json.JSONObject

/**
 *
 * This class is used to parse the api instead of FanboxParser(Deprecated).
 * One function fetches one api.
 */

class ParserRouter {
    private val TAG = "ParserRouter"

    val mainAPI = "https://fanbox.pixiv.net/api"
    val fanboxAPI = "https://api.fanbox.cc"
    private fun <T> requestWithCookieRefreshing(
        api: String,
        url: String,
        onResp: URLRequestor.OnResponseListener<T>,
        onDone: URLRequestor.OnDoneListener<T>? = null
    ): URLRequestor<T> {
        val refer = "${api}"
        val headers = HashMap<String, String>()
        headers["Refer"] = refer
        headers["Origin"] = "${fanboxAPI}"
        return URLRequestor(url, URLRequestor.OnResponseListener {
            var obj: T? = null


            try {
                Constants.Cookie = it.header("Set-Cookie", Constants.Cookie)
                obj = onResp.onResponse(it)
            } catch (ex: Exception) {
                Log.e("${TAG}/Req", "EXCEPTION: " + ex)
            }
            return@OnResponseListener obj
        }, headers, onDone)
    }



    fun getSupportingList(): Array<SupportingItem> {
        val url = "${fanboxAPI}/plan.listSupporting"
        val ur = requestWithCookieRefreshing<JSONObject?>(fanboxAPI, url, {
            val result = it.body!!.string()
            Log.d("[Sync SUCCEEDED]", "requestWithCookie:$url Body:${result}")

            JSONObject(result)
        })?.sync()
        val resp=ur.getReturnValue()
        try {
            val items = resp?.getJSONArray("body")
            val list = mutableListOf<SupportingItem>()
            for (i in 0 until items!!.length()) {
                val item = items.getJSONObject(i)
                val creator = item.getJSONObject("user").getString("name")
                val id = item.getString("id")
                val plan = item.getString("fee")
                val title = item.getString("title")
                val desc = item.getString("description")
                val paymentMethod = item.getString("paymentMethod")
                val coverImgURL = item.getString("coverImageUrl")
                list.add(SupportingItem("", creator, plan, title, desc, paymentMethod, coverImgURL))
            }
            return list.toTypedArray()
        }catch (e: JSONException){
            Log.e(TAG, "EXCEPTION: $e")
            return arrayOf()
        }
    }
}
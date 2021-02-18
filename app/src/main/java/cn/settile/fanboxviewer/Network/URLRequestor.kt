package cn.settile.fanboxviewer.Network

import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class URLRequestor(url: String, callback: OnResponseListener) {
    var req: Request;
    var resp: Response? = null
    var rv: Boolean? = null

    init {
        req = Request.Builder()
                .url(url)
                .build()
        try {
            resp = Common.client.newCall(req).execute()
            if (resp != null) {
                rv = callback.onResponse(resp!!)
            }
        } catch (e: Exception) {
        }
    }

    fun getReturnValue(): Boolean? {
        return rv
    }

    fun getBody(): Document? {
        var document: Document? = null
        try {
            document = Jsoup.parse(resp?.body?.string())
        } catch (e: Exception) {
        }
        return document
    }

    fun interface OnResponseListener {
        fun onResponse(resp: Response): Boolean?
    }
}
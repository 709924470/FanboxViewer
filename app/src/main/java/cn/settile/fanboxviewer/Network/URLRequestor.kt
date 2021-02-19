package cn.settile.fanboxviewer.Network

import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document



class URLRequestor<T>(url: String, callback: OnResponseListener<T>, headers: Map<String, String>?) {
    var req: Request;
    var resp: Response? = null
    var rv: T? = null

    init {
        val reqb = Request.Builder();

        reqb.url(url);
        if (headers != null) {
            for (header in headers) {
                reqb.addHeader(header.key, header.value)
            }
        }
        req = reqb.build()
        try {
            resp = Common.getClientInstance().newCall(req).execute()
            if (resp != null) {
                rv = callback.onResponse(resp!!)
            }
        } catch (e: Exception) {
        }
    }

    fun getReturnValue(): T? {
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

    fun interface OnResponseListener <T>{
        fun onResponse(resp: Response): T?
    }
}
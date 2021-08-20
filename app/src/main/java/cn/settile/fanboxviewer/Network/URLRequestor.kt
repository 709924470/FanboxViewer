package cn.settile.fanboxviewer.Network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


class URLRequestor<T>(url: String, callback: OnResponseListener<T>, headers: Map<String, String>?) {
    private lateinit var req: Request;
    private var resp: Response? = null
    private var rv: T? = null

    init {
        MainScope().launch {
            initRequestorAsync(url, callback, headers)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun initRequestorAsync(
        url: String,
        callback: OnResponseListener<T>,
        headers: Map<String, String>?
    ) {
        withContext(Dispatchers.IO) {
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

    fun interface OnResponseListener<T> {
        fun onResponse(resp: Response): T?
    }
}
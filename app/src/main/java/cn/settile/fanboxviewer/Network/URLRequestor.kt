package cn.settile.fanboxviewer.Network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


class URLRequestor<T>(
    val url: String,
    val onResp: OnResponseListener<T>,
    val headers: Map<String, String>? = null,
    val onDone: OnDoneListener<T>? = null
) {
    private lateinit var req: Request;
    private var resp: Response? = null
    private var rv: T? = null
    var done = false
    var isCalled = false
    fun async(): URLRequestor<T> {
        if (isCalled) throw Exception("URLRequestor has been called.")
        isCalled = true
        MainScope().launch {
            withContext(Dispatchers.IO) {
                syncI()
            }
        }

        return this
    }

    fun sync(): URLRequestor<T> {
        if (isCalled) throw Exception("URLRequestor has been called.")
        isCalled = true
        return syncI()
    }

    private fun syncI(): URLRequestor<T> {
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
                rv = onResp.onResponse(resp!!)
            }
        } catch (e: Exception) {
        }
        done=true
        onDone?.onDone(this)
        return this
    }

    fun isDone(): Boolean {
        return done
    }

    fun getReturnValue(): T? {
        if (isDone()) {
            return rv
        } else {
            throw Exception("Requestor Is Not Done.")
        }
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

    fun interface OnDoneListener<T> {
        fun onDone(req: URLRequestor<T>)
    }
}
package cn.settile.fanboxviewer.Network

import android.app.DownloadManager
import android.net.Uri
import cn.settile.fanboxviewer.Util.Constants.DOWNLOAD_PATH
import java.io.File

class DownloadRequestor(var dm: DownloadManager, var path: String = DOWNLOAD_PATH) {
    constructor(manager: DownloadManager) : this(manager, DOWNLOAD_PATH)

    fun download(url: String, name: String) {
        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(File(path + File.separatorChar + name)))
        request.setTitle(name)
        request.setAllowedOverMetered(true)
        request.setAllowedOverRoaming(true)
        dm.enqueue(request)
    }

    fun downloadWithCookie(url: String, name: String, cookie: String) {
        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.addRequestHeader("Cookie", cookie)
        request.setDestinationUri(Uri.fromFile(File(path + File.separatorChar + name)))
        request.setTitle(name)
        request.setAllowedOverMetered(true)
        request.setAllowedOverRoaming(true)
        dm.enqueue(request)
    }

}
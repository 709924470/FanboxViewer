package cn.settile.fanboxviewer.Network;

import android.util.Log;
import android.webkit.CookieManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.settile.fanboxviewer.Util.Constants;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class WebViewCookieHandler implements CookieJar {
    private static final String TAG = "wvCookieHdl";
    private CookieManager mCookieManager = CookieManager.getInstance();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        String urlString = url.toString();

        for (Cookie cookie : cookies) {
            mCookieManager.setCookie(urlString, cookie.toString());
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        Log.d(TAG, "loadForRequest() called with: url = [" + url + "]");
        String urlString = url.toString();
        String cookiesString = mCookieManager.getCookie(urlString);

        if (cookiesString != null && !cookiesString.isEmpty()) {
            String[] cookieHeaders = cookiesString.split(";");
            List<Cookie> cookies = new ArrayList<>(cookieHeaders.length);

            for (String header : cookieHeaders) {
                cookies.add(Cookie.parse(url, header));
            }

            Constants.Cookie = cookiesString;
            return cookies;
        }

        return Collections.emptyList();
    }

    public String loadForRequest(String url){
        Constants.Cookie = mCookieManager.getCookie(url);
        return Constants.Cookie;
    }
}
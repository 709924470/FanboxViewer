package cn.settile.fanboxviewer.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PostDetailViewModel : ViewModel() {
    val user_name: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    val user_id: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    val url_user_icon: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }

    val url_article: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    val article_title: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    val article_fee: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    val article_time: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    val url_cover: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }

    val article_is_loaded: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }

    fun update_article_info(url: String, title: String, time: String, fee: String, coverURL: String) {
        url_article.postValue(url)
        article_title.postValue(title)
        article_fee.postValue(fee)
        url_cover.postValue(coverURL)
        article_time.postValue(time)
    }

    fun update_user_info(userName: String, userId: String, userIconUrl: String) {
        user_name.postValue(userName)
        user_id.postValue(userId)
        url_user_icon.postValue(userIconUrl)
    }

    fun update_article_state(state: Boolean) {
        article_is_loaded.postValue(state)
    }
}
package cn.settile.fanboxviewer.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val is_logged_in: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }
    val is_online: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }

    val user_name: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    val user_id: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    val user_icon_url: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }

    fun update_user_info(username: String, userid: String, usericonurl: String) {
        user_name.postValue(username)
        user_id.postValue(userid)
        user_icon_url.postValue(usericonurl)
    }

    fun update_is_logged_in(p0: Boolean) {
        is_logged_in.postValue(p0)
    }

    fun update_is_online(p0: Boolean) {
        is_online.postValue(p0)
    }
}
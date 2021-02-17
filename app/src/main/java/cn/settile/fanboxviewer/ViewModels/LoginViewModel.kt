package cn.settile.fanboxviewer.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    val is_loaded by lazy {
        MutableLiveData(false)
    }

    fun update_isloaded(p0: Boolean) {
        is_loaded.postValue(p0);
    }
}
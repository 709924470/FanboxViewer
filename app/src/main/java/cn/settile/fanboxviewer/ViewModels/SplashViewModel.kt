package cn.settile.fanboxviewer.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.settile.fanboxviewer.Util.Constants

class SplashViewModel : ViewModel() {
    val storage_permission_state: MutableLiveData<Constants.CheckItemState> by lazy {
        MutableLiveData(Constants.CheckItemState.UNKNOW)
    }
    val cookie_state: MutableLiveData<Constants.CheckItemState> by lazy {
        MutableLiveData(Constants.CheckItemState.UNKNOW)
    }
    val network_state: MutableLiveData<Constants.CheckItemState> by lazy {
        MutableLiveData(Constants.CheckItemState.UNKNOW)
    }

    fun update_storage_permission_state(checkItemState: Constants.CheckItemState){
        storage_permission_state.postValue(checkItemState)
    }
    fun update_cookie_state(checkItemState: Constants.CheckItemState){
        cookie_state.postValue(checkItemState)
    }
    fun update_network_state(checkItemState: Constants.CheckItemState){
        network_state.postValue(checkItemState)
    }

}
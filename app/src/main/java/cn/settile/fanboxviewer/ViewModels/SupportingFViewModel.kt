package cn.settile.fanboxviewer.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.settile.fanboxviewer.Network.Bean.SupportingItem
import cn.settile.fanboxviewer.Network.ParserRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupportingFViewModel : ViewModel() {

    val supportingList: MutableLiveData<Array<SupportingItem>> by lazy {
        MutableLiveData<Array<SupportingItem>>()
    }

    val isLoading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    init {
        refresh()
    }

    fun fetchSupportingList() {
        GlobalScope.launch(Dispatchers.IO){
            supportingList.postValue(ParserRouter().getSupportingList())
            isLoading.postValue(false)
        }
    }

    fun refresh(){
        fetchSupportingList()
        isLoading.value = true
    }


    // TODO: Implement the ViewModel
}
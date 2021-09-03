package cn.settile.fanboxviewer.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.settile.fanboxviewer.Network.Bean.CardItem

class UserDetailViewModel : ViewModel() {
    val user_posts by lazy {
        MutableLiveData(mutableListOf<CardItem>())
    }
}
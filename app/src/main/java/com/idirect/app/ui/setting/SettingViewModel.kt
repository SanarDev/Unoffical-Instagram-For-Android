package com.idirect.app.ui.setting

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.User
import com.idirect.app.ui.login.LoginActivity
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import javax.inject.Inject
import kotlin.reflect.KClass

class SettingViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    private lateinit var userInfo: User
    val imageProfileUrl = ObservableField<String>()
    val accountFullName = ObservableField<String>()
    val accountBio = ObservableField<String>()
    val accountUserName = ObservableField<String>()
    val postCount = ObservableField<String>("0")
    val followersCount = ObservableField<String>("0")
    val followingCount = ObservableField<String>("0")
    val isEnableNotification = ObservableField<Boolean>(false)
    val isEnableSeenMessage = ObservableField<Boolean>(false)
    val intentEvent = MutableLiveData<Pair<KClass<out AppCompatActivity>, Bundle?>>()

    fun onLogOutClick(v: View) {

    }

    init {
        val user = mUseCase.getUserData()!!
        imageProfileUrl.set(user.profilePicUrl)
        accountFullName.set(user.fullName)
        accountUserName.set(user.username)
        isEnableNotification.set(mUseCase.isNotificationEnable)
        isEnableSeenMessage.set(mUseCase.isSeenMessageEnable)

        mUseCase.getMe().observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                userInfo = it.data!!.user
                initAccountData()
            }
        }
    }

    private fun initAccountData(){
        imageProfileUrl.set(userInfo.hdProfilePicUrlInfo.url)
        accountBio.set(userInfo.biography)
        postCount.set(userInfo.mediaCount.toString())
        followersCount.set(userInfo.followerCount.toString())
        followingCount.set(userInfo.followingCount.toString())
    }
    fun onNotificationChanged(checked: Boolean) {
        mUseCase.isNotificationEnable = checked
    }
    fun onSeenMessageChanged(checked: Boolean) {
        mUseCase.isSeenMessageEnable = checked
    }

    fun logout() {
        mUseCase.logout().observeForever {
            intentEvent.postValue(Pair(LoginActivity::class,null))
        }
    }


}
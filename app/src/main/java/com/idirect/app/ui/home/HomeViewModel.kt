package com.idirect.app.ui.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramFeedTimeLineResponse
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class HomeViewModel @Inject constructor(application: Application,var mUseCase: UseCase):BaseViewModel(application) {

    val postsLiveData = MutableLiveData<Resource<InstagramFeedTimeLineResponse>>()

    init {
        mUseCase.getTimelinePosts().observeForever {
            if(it.status == Resource.Status.SUCCESS){
                for(item in it.data!!.feedItems){
                    if(item.mediaOrAd == null){
                        it.data!!.feedItems.remove(item)
                        break
                    }
                }
            }
            postsLiveData.value = it
        }
    }


    fun unlikePost(id: String) {
        mUseCase.unlikePost(id)
    }

    fun likePost(id: String) {
        mUseCase.likePost(id)
    }

    fun getStandardVideoSize(width: Int, height: Int): Array<Int> {
        val screenWidth = DisplayUtils.getScreenWidth()
        val screenHeight = DisplayUtils.getScreenHeight()
        var standardHeight = (height * screenWidth) / width
        if(standardHeight > width && standardHeight > screenHeight/3){
            standardHeight = screenWidth
        }
        return arrayOf(screenWidth,standardHeight)
    }
}
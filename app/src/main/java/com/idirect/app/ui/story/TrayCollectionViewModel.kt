package com.idirect.app.ui.story

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseViewModel
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.model.story.Tray
import com.sanardev.instagramapijava.response.IGTimeLineStoryResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.ResponseBody
import javax.inject.Inject

class TrayCollectionViewModel @Inject constructor(application: Application, val mUseCase: UseCase) :
    BaseViewModel(application) {

    private val storiesDataPrivate = MutableLiveData<Resource<List<Tray>>>()
    val storiesData:LiveData<Resource<List<Tray>>> get() = storiesDataPrivate

    @SuppressLint("CheckResult")
    fun getStoryData(userId: Long, isSingle: Boolean, loadFromCache: Boolean = false) {
        if (loadFromCache) {
            if (storiesData.value != null) {
                return
            }
        }
        storiesDataPrivate.value = Resource.loading()
        if (isSingle) {
            mUseCase.getStoryMedia(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    storiesDataPrivate.value = Resource.success(arrayOf(it.reels[userId]!!).toList())
                }, {}, {})
        } else {
            mUseCase.getTimelineStory()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    storiesDataPrivate.value = Resource.success(it.tray)
                }, {
                    Log.i("TEST", "TEST")
                }, {})
        }
    }
}
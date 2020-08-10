package com.idirect.app.ui.postcomments

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramCommentResponse
import com.idirect.app.datasource.model.response.InstagramLoggedUser
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class CommentsViewModel @Inject constructor(application: Application,var mUseCase: UseCase):BaseViewModel(application){

    val comments = MutableLiveData<Resource<InstagramCommentResponse>>()

    private lateinit var mediaId:String
    fun init(mediaId:String){
        this.mediaId = mediaId

        mUseCase.getPostComments(mediaId).observeForever {
            comments.value = it
        }
    }

    fun getUser(): InstagramLoggedUser {
        return mUseCase.getUserData()!!
    }

}
package com.idirect.app.ui.postcomments

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramCommentResponse
import com.idirect.app.datasource.model.response.InstagramLoggedUser
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class CommentsViewModel @Inject constructor(application: Application,var mUseCase: UseCase):BaseViewModel(application){

    private val _comments = MutableLiveData<Resource<InstagramCommentResponse>>()
    private var instagramCommentResponse:InstagramCommentResponse?=null
    private lateinit var mediaId:String
    val comments = Transformations.map(_comments){
        if(it.status == Resource.Status.SUCCESS){
            if(instagramCommentResponse == null){
                instagramCommentResponse = it.data!!
            }else{
                if(instagramCommentResponse!!.nextMinId == it.data!!.nextMinId){
                    return@map it
                }
                instagramCommentResponse!!.comments.addAll(it.data!!.comments)
                it.data!!.comments = instagramCommentResponse!!.comments
                instagramCommentResponse = it.data!!
            }
        }
        return@map it
    }
    fun init(mediaId:String){
        this.mediaId = mediaId

        mUseCase.getPostComments(_comments,mediaId)
    }

    fun getUser(): InstagramLoggedUser {
        return mUseCase.getUserData()!!
    }

    fun loadMoreComments() {
        mUseCase.loadMoreComment(_comments,instagramCommentResponse!!.mediaId,instagramCommentResponse!!.nextMinId)
    }

    fun unlikeComment(pk: Long) {
        mUseCase.unlikeComment(pk.toString())
    }
    fun likeComment(pk: Long) {
        mUseCase.likeComment(pk.toString())
    }

}
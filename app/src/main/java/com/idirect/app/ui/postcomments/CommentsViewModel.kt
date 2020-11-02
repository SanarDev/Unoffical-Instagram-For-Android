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
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.model.login.IGLoggedUser
import com.sanardev.instagramapijava.response.IGCommentsResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.ResponseBody
import javax.inject.Inject

class CommentsViewModel @Inject constructor(application: Application):BaseViewModel(application){

    private val instaClient = InstaClient.getInstanceCurrentUser(application.applicationContext)
    private val _comments = MutableLiveData<Resource<IGCommentsResponse>>()
    private var instagramCommentResponse:IGCommentsResponse?=null
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
        instaClient.commentProcessor.getPostComments(mediaId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _comments.value = Resource.success(it)
            },{},{})
    }

    fun getUser(): IGLoggedUser {
        return instaClient.loggedUser
    }

    fun loadMoreComments() {
        instaClient.commentProcessor.getPostComments(mediaId,instagramCommentResponse!!.nextMinId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _comments.value = Resource.success(it)
            },{},{})
    }

    fun unlikeComment(pk: Long) {
        instaClient.commentProcessor.unlikeComment(pk.toString()).subscribe({},{},{})
    }
    fun likeComment(pk: Long) {
        instaClient.commentProcessor.likeComment(pk.toString()).subscribe()
    }

}
package com.idirect.app.ui.postcomments

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseViewModel
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.model.login.IGLoggedUser
import com.sanardev.instagramapijava.response.IGCommentsResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class CommentsViewModel @Inject constructor(application: Application,val mUseCase: UseCase):BaseViewModel(application){

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
        mUseCase.getPostsComments(mediaId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _comments.value = Resource.success(it)
            },{},{})
    }

    fun getUser(): IGLoggedUser {
        return mUseCase.getLoggedUser()!!
    }

    fun loadMoreComments() {
        mUseCase.getPostsMoreComments(mediaId,instagramCommentResponse!!.nextMinId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _comments.value = Resource.success(it)
            },{},{})
    }

    fun unlikeComment(pk: Long) {
        mUseCase.unlikeComment(pk).subscribe({},{},{})
    }
    fun likeComment(pk: Long) {
        mUseCase.likeComment(pk).subscribe()
    }

}
package com.idirect.app.ui.search

import android.app.Application
import android.os.Handler
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramRecipients
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import javax.inject.Inject

class SearchViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {


    private var searchWord: String = ""
    private val mHandler = Handler()
    private var lastSearchTimestamp = 0.toLong()
    private val runnable = Runnable {
        if (System.currentTimeMillis() - 2000 > lastSearchTimestamp) {
            mUseCase.getRecipients(result, searchWord)
        }
    }
    private val result = MutableLiveData<Resource<InstagramRecipients>>()
    val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.SUCCESS) {
            for (index in it.data!!.recipients.indices) {
                val item = it.data!!.recipients[index]
                if (item.thread != null && item.thread.users.isEmpty()) {
                    it.data!!.recipients.removeAt(index)
                    break
                }
            }
        }
        return@map it
    }

    init {
        mUseCase.getRecipients(result)

    }

    fun getRecipients(query: String = "") {
        mUseCase.getRecipients(result, query)
    }


    fun search(word: String) {
        if (searchWord == word) {
            return
        }
        lastSearchTimestamp = System.currentTimeMillis()
        mHandler.postDelayed(runnable, 3000)
        searchWord = word
        result.postValue(Resource.loading())
    }
}
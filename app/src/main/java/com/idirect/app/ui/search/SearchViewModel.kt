package com.idirect.app.ui.search

import android.app.Application
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseViewModel
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.response.IGRecipientsResponse
import javax.inject.Inject

class SearchViewModel @Inject constructor(application: Application, val mUseCase: UseCase) :
    BaseViewModel(application) {

    private var searchWord: String = ""
    private val mHandler = Handler()
    private var lastSearchTimestamp = 0.toLong()
    private val result = MutableLiveData<Resource<IGRecipientsResponse>>()

    private val runnable = Runnable {
        if (System.currentTimeMillis() - 2000 > lastSearchTimestamp) {
            getRecipients(searchWord)
        }
    }
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
        getRecipients()
    }

    fun getRecipients(query: String = "") {
        mUseCase.getRecipient(query)
            .subscribe({
                result.value = Resource.success(it)
            }, {}, {})
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
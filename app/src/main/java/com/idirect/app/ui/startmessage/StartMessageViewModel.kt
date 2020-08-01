package com.idirect.app.ui.startmessage

import android.app.Application
import android.os.Handler
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramRecipients
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import javax.inject.Inject

class StartMessageViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {


    private var searchWord: String = ""
    private var lastSearchedWord: String = ""
    private val mHandler = Handler()
    private val result = MediatorLiveData<Resource<InstagramRecipients>>()
    val liveData = Transformations.map(result) {
        return@map it
    }

    init {
        mUseCase.getRecipients(result)
        Thread {
            while (true) {
                mHandler.post {
                    if (searchWord != lastSearchedWord) {
                        lastSearchedWord = searchWord
                        mUseCase.getRecipients(result, searchWord)
                    }
                }
                Thread.sleep(5000)
            }
        }.start()
    }

    fun getRecipients(query: String = "") {
        mUseCase.getRecipients(result, query)
    }


    fun onSearch(s: CharSequence, start: Int, before: Int, count: Int) {
        searchWord = s.toString()
        result.postValue(Resource.loading())
    }
}
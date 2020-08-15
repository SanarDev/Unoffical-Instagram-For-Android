package com.idirect.app.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import dagger.android.support.DaggerFragment
import com.idirect.app.BR
import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.idirect.app.di.DaggerViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseFragment<B: ViewDataBinding,VM: BaseViewModel> :DaggerFragment(){

    lateinit var binding: B
    lateinit var viewModel: VM

    @Inject
    internal lateinit var viewModelFactory: DaggerViewModelFactory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, layoutRes(), container, false
        )
        viewModel = ViewModelProvider(this, viewModelFactory).get(getViewModelClass())
        binding.setVariable(BR.viewModel,viewModel)
        val view = binding.root
        return view
    }
    open fun onKeyboardOpen(){
    }
    open fun onKeyboardHide(){
    }

    abstract fun getViewModelClass(): Class<VM>
    abstract fun layoutRes():Int
    abstract fun getNameTag():String
}
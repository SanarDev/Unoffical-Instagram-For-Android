package com.sanardev.instagrammqtt.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sanardev.instagrammqtt.di.DaggerViewModelFactory
import com.sanardev.instagrammqtt.BR
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

 abstract class BaseActivity<B:ViewDataBinding,VM:BaseViewModel> : AppCompatActivity(),
    HasSupportFragmentInjector {

    lateinit var binding: B
    lateinit var viewModel: VM

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    internal lateinit var viewModelFactory: DaggerViewModelFactory

    override fun supportFragmentInjector() = fragmentInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutRes())
        viewModel = ViewModelProvider(this, viewModelFactory).get(getViewModelClass())
        binding.setVariable(BR.viewModel, viewModel)
    }

    abstract fun layoutRes(): Int
    abstract fun getViewModelClass(): Class<VM>
}
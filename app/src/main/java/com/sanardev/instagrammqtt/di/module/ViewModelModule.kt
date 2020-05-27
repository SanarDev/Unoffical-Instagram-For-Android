package com.sanardev.anemanagement.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sanardev.anemanagement.di.DaggerViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory


//    @Binds
//    @IntoMap
//    @ViewModelKey(CallLogViewModel::class)
//    abstract fun callLogViewModel(callLogViewModel: CallLogViewModel):ViewModel
}
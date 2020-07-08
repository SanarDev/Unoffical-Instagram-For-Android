package com.sanardev.instagrammqtt.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sanardev.instagrammqtt.di.DaggerViewModelFactory
import com.sanardev.instagrammqtt.di.ViewModelKey
import com.sanardev.instagrammqtt.ui.direct.DirectViewModel
import com.sanardev.instagrammqtt.ui.login.LoginViewModel
import com.sanardev.instagrammqtt.ui.main.MainViewModel
import com.sanardev.instagrammqtt.ui.startmessage.StartMessageViewModel
import com.sanardev.instagrammqtt.ui.twofactor.TwoFactorActivity
import com.sanardev.instagrammqtt.ui.twofactor.TwoFactorViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun mainViewModel(mainViewModel: MainViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun loginViewModel(loginViewModel: LoginViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TwoFactorViewModel::class)
    abstract fun twoFactorViewModel(twoFactorViewModel: TwoFactorViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DirectViewModel::class)
    abstract fun directViewModel(directViewModel: DirectViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StartMessageViewModel::class)
    abstract fun startMessageViewModel(startMessageViewModel: StartMessageViewModel):ViewModel

}
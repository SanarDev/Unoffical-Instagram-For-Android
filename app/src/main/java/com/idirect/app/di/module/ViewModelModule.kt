package com.idirect.app.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.idirect.app.di.DaggerViewModelFactory
import com.idirect.app.di.ViewModelKey
import com.idirect.app.ui.direct.DirectViewModel
import com.idirect.app.ui.fullscreen.FragmentCollectionViewModel
import com.idirect.app.ui.fullscreen.FullScreenViewModel
import com.idirect.app.ui.login.LoginViewModel
import com.idirect.app.ui.main.MainViewModel
import com.idirect.app.ui.playvideo.PlayVideoViewModel
import com.idirect.app.ui.setting.SettingViewModel
import com.idirect.app.ui.startmessage.StartMessageViewModel
import com.idirect.app.ui.twofactor.TwoFactorViewModel
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

    @Binds
    @IntoMap
    @ViewModelKey(FullScreenViewModel::class)
    abstract fun fullScreenViewModel(fullScreenViewModel: FullScreenViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayVideoViewModel::class)
    abstract fun playVideoViewModel(playVideoViewModel: PlayVideoViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FragmentCollectionViewModel::class)
    abstract fun fragmentCollectionViewModel(fragmentCollectionViewModel: FragmentCollectionViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    abstract fun settingViewModel(settingViewModel: SettingViewModel):ViewModel

}
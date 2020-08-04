package com.idirect.app.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.idirect.app.di.DaggerViewModelFactory
import com.idirect.app.di.ViewModelKey
import com.idirect.app.ui.direct.DirectViewModel
import com.idirect.app.ui.fullscreen.FragmentCollectionViewModel
import com.idirect.app.ui.fullscreen.FullScreenViewModel
import com.idirect.app.ui.inbox.InboxViewModel
import com.idirect.app.ui.login.LoginViewModel
import com.idirect.app.ui.main.ShareViewModel
import com.idirect.app.ui.playvideo.PlayVideoViewModel
import com.idirect.app.ui.setting.SettingViewModel
import com.idirect.app.ui.startmessage.StartMessageViewModel
import com.idirect.app.ui.twofactor.TwoFactorViewModel
import com.idirect.app.ui.userprofile.UserProfileViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(ShareViewModel::class)
    abstract fun mainViewModel(shareViewModel: ShareViewModel):ViewModel

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

    @Binds
    @IntoMap
    @ViewModelKey(InboxViewModel::class)
    abstract fun inboxViewModel(inboxViewModel: InboxViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DirectViewModel::class)
    abstract fun directViewModel(directViewModel: DirectViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    abstract fun userProfileViewModel(userProfileViewModel: UserProfileViewModel):ViewModel

}
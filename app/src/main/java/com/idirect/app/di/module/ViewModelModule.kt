package com.idirect.app.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.idirect.app.di.DaggerViewModelFactory
import com.idirect.app.di.ViewModelKey
import com.idirect.app.ui.direct.DirectViewModel
import com.idirect.app.ui.forward.ForwardViewModel
import com.idirect.app.ui.fullscreen.FullScreenViewModel
import com.idirect.app.ui.home.HomeViewModel
import com.idirect.app.ui.inbox.InboxViewModel
import com.idirect.app.ui.login.LoginViewModel
import com.idirect.app.ui.main.ShareViewModel
import com.idirect.app.ui.playvideo.PlayVideoViewModel
import com.idirect.app.ui.postcomments.CommentsViewModel
import com.idirect.app.ui.posts.PostsViewModel
import com.idirect.app.ui.profile.ProfileViewModel
import com.idirect.app.ui.setting.SettingViewModel
import com.idirect.app.ui.search.SearchViewModel
import com.idirect.app.ui.singlepost.SinglePostViewModel
import com.idirect.app.ui.story.StoryViewModel
import com.idirect.app.ui.story.TrayCollectionViewModel
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
    @ViewModelKey(SearchViewModel::class)
    abstract fun startMessageViewModel(startMessageViewModel: SearchViewModel):ViewModel

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

    @Binds
    @IntoMap
    @ViewModelKey(PostsViewModel::class)
    abstract fun postsViewModel(postsViewModel: PostsViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommentsViewModel::class)
    abstract fun commentsViewModel(commentsViewModel: CommentsViewModel):ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun homeViewModel(homeViewModel: HomeViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StoryViewModel::class)
    abstract fun storyItemViewModel(storyViewModel: StoryViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrayCollectionViewModel::class)
    abstract fun storyViewModel(trayCollectionViewModel: TrayCollectionViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForwardViewModel::class)
    abstract fun forwardViewModel(forwardViewModel: ForwardViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun profileViewModel(profileViewModel: ProfileViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SinglePostViewModel::class)
    abstract fun singlePostViewModel(singlePostViewModel: SinglePostViewModel):ViewModel
}
package com.idirect.app.di.module

import com.idirect.app.ui.direct.FragmentDirect
import com.idirect.app.ui.home.FragmentHome
import com.idirect.app.ui.posts.FragmentCollection
import com.idirect.app.ui.inbox.FragmentInbox
import com.idirect.app.ui.postcomments.CommentsFragment
import com.idirect.app.ui.posts.PostsFragment
import com.idirect.app.ui.startmessage.StartMessageFragment
import com.idirect.app.ui.userprofile.UserProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentsModule {

    @ContributesAndroidInjector
    abstract fun contributeFragmentCollection(): FragmentCollection

    @ContributesAndroidInjector
    abstract fun contributeFragmentInbox(): FragmentInbox

    @ContributesAndroidInjector
    abstract fun contributeFragmentDirect(): FragmentDirect

    @ContributesAndroidInjector
    abstract fun contributeStartMessageFragment(): StartMessageFragment

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragment(): UserProfileFragment

    @ContributesAndroidInjector
    abstract fun contributePostsFragment(): PostsFragment

    @ContributesAndroidInjector
    abstract fun contributeCommentsFragment(): CommentsFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentHome(): FragmentHome
}
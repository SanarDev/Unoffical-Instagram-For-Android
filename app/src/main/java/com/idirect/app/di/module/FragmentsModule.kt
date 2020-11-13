package com.idirect.app.di.module

import com.idirect.app.ui.direct.FragmentDirect
import com.idirect.app.ui.forward.ForwardFragment
import com.idirect.app.ui.home.FragmentHome
import com.idirect.app.ui.posts.FragmentCollection
import com.idirect.app.ui.inbox.FragmentInbox
import com.idirect.app.ui.postcomments.CommentsFragment
import com.idirect.app.ui.posts.PostsFragment
import com.idirect.app.ui.profile.FragmentProfile
import com.idirect.app.ui.search.FragmentSearch
import com.idirect.app.ui.singlepost.FragmentSinglePost
import com.idirect.app.ui.story.FragmentTrayCollection
import com.idirect.app.ui.story.FragmentStory
import com.idirect.app.ui.story.question.FragmentQuestion
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
    abstract fun contributeStartMessageFragment(): FragmentSearch

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragment(): UserProfileFragment

    @ContributesAndroidInjector
    abstract fun contributePostsFragment(): PostsFragment

    @ContributesAndroidInjector
    abstract fun contributeCommentsFragment(): CommentsFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentHome(): FragmentHome

    @ContributesAndroidInjector
    abstract fun contributeFragmentStoryItem(): FragmentStory

    @ContributesAndroidInjector
    abstract fun contributeFragmentStory(): FragmentTrayCollection

    @ContributesAndroidInjector
    abstract fun contributeForwardFragment(): ForwardFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentProfile(): FragmentProfile

    @ContributesAndroidInjector
    abstract fun contributeFragmentSinglePost(): FragmentSinglePost

    @ContributesAndroidInjector
    abstract fun contributeFragmentQuestion(): FragmentQuestion
}
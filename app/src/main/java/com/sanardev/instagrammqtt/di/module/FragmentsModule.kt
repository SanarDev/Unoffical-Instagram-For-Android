package com.sanardev.instagrammqtt.di.module

import com.sanardev.instagrammqtt.ui.fullscreen.FragmentCollection
import com.sanardev.instagrammqtt.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentsModule {

    @ContributesAndroidInjector
    abstract fun contributeFragmentCollection(): FragmentCollection

}
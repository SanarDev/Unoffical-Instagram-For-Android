package com.idirect.app.di.module

import com.idirect.app.ui.fullscreen.FragmentCollection
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentsModule {

    @ContributesAndroidInjector
    abstract fun contributeFragmentCollection(): FragmentCollection

}
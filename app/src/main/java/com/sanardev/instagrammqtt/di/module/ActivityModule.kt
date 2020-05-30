package com.sanardev.instagrammqtt.di.module

import com.sanardev.instagrammqtt.ui.login.LoginActivity
import com.sanardev.instagrammqtt.ui.main.MainActivity
import com.sanardev.instagrammqtt.ui.twofactor.TwoFactorActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun contributeTwoFactorActivity(): TwoFactorActivity
}
package com.sanardev.instagrammqtt.di.module

import com.sanardev.instagrammqtt.ui.direct.DirectActivity
import com.sanardev.instagrammqtt.ui.fullscreen.FullScreenActivity
import com.sanardev.instagrammqtt.ui.login.LoginActivity
import com.sanardev.instagrammqtt.ui.main.MainActivity
import com.sanardev.instagrammqtt.ui.playvideo.PlayVideoActivity
import com.sanardev.instagrammqtt.ui.setting.SettingActivity
import com.sanardev.instagrammqtt.ui.startmessage.StartMessageActivity
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

    @ContributesAndroidInjector
    abstract fun contributeDirectActivity(): DirectActivity

    @ContributesAndroidInjector
    abstract fun contributeStartMessageActivity():StartMessageActivity

    @ContributesAndroidInjector
    abstract fun contributeFullScreenActivity():FullScreenActivity

    @ContributesAndroidInjector
    abstract fun contributePlayVideoActivity():PlayVideoActivity

    @ContributesAndroidInjector
    abstract fun contributeSettingActivity():SettingActivity
}
package com.idirect.app.di.module

import com.idirect.app.ui.direct.DirectActivity
import com.idirect.app.ui.fullscreen.FullScreenActivity
import com.idirect.app.ui.login.LoginActivity
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.playvideo.PlayVideoActivity
import com.idirect.app.ui.setting.SettingActivity
import com.idirect.app.ui.startmessage.StartMessageActivity
import com.idirect.app.ui.twofactor.TwoFactorActivity
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
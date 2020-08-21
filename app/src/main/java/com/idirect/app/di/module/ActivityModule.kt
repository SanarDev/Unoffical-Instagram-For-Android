package com.idirect.app.di.module

import com.idirect.app.ui.fullscreen.FullScreenFragment
import com.idirect.app.ui.login.LoginActivity
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.playvideo.PlayVideoActivity
import com.idirect.app.ui.setting.SettingActivity
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
    abstract fun contributeFullScreenActivity():FullScreenFragment

    @ContributesAndroidInjector
    abstract fun contributePlayVideoActivity():PlayVideoActivity

    @ContributesAndroidInjector
    abstract fun contributeSettingActivity():SettingActivity
}
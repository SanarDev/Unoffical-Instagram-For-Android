package com.idirect.app.di.module

import com.idirect.app.service.fbns.FbnsService
import com.idirect.app.service.realtime.RealTimeService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeFbnsService(): FbnsService

    @ContributesAndroidInjector
    abstract fun contributeRealTimeService(): RealTimeService

}
package com.idirect.app.di.module

import com.idirect.app.fbns.service.FbnsService
import com.idirect.app.realtime.service.RealTimeService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeFbnsService(): FbnsService

    @ContributesAndroidInjector
    abstract fun contributeRealTimeService(): RealTimeService

}
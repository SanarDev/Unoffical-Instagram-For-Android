package com.sanardev.instagrammqtt.di.module

import com.sanardev.instagrammqtt.service.fbns.FbnsService
import com.sanardev.instagrammqtt.service.realtime.RealTimeService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeFbnsService(): FbnsService

    @ContributesAndroidInjector
    abstract fun contributeRealTimeService(): RealTimeService

}
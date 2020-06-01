package com.sanardev.instagrammqtt.core

import android.app.Activity
import android.app.Application
import android.app.Service
import com.sanardev.instagrammqtt.di.component.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import net.igenius.mqttservice.MQTTService
import javax.inject.Inject

class BaseApplication : Application() , HasActivityInjector, HasServiceInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)

        MQTTService.NAMESPACE = "com.sanardev.instagrammqtt.receiver"; //or BuildConfig.APPLICATION_ID;
        MQTTService.KEEP_ALIVE_INTERVAL = 60; //in seconds
        MQTTService.CONNECT_TIMEOUT = 30; //in seconds
    }

}
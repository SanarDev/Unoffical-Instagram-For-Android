package com.sanardev.anemanagement.di.component

import android.app.Application
import com.sanardev.anemanagement.di.module.*
import com.sanardev.instagrammqtt.core.BaseApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Component(modules = [BroadcastReceiverModule::class,ServiceModule::class,FragmentsModule::class,ActivityModule::class, AppModule::class, ViewModelModule::class, AndroidSupportInjectionModule::class])
@Singleton
interface AppComponent{

    @Component.Builder
    interface Builder{
        @BindsInstance
        fun application(application: Application):Builder

        fun build():AppComponent
    }
    fun inject(app: BaseApplication)
}
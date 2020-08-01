package com.idirect.app.di.component

import android.app.Application
import com.idirect.app.di.module.*
import com.idirect.app.core.BaseApplication
import com.idirect.app.di.module.ApiModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Component(modules = [BroadcastReceiverModule::class,ApiModule::class,ServiceModule::class,FragmentsModule::class,ActivityModule::class, AppModule::class, ViewModelModule::class, AndroidSupportInjectionModule::class])
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
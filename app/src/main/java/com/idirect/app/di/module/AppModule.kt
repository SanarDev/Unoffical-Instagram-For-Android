package com.idirect.app.di.module

import android.app.Application
import android.content.Context
import android.os.Handler
import com.google.gson.Gson
import com.idirect.app.datasource.local.MessageDataSource
import com.idirect.app.datasource.remote.InstagramRemote
import com.idirect.app.manager.PlayManager
import com.idirect.app.datasource.repository.InstagramRepository
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.CookieUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class AppModule {

    protected val DB_NAME = "db_app"

    @Provides
    @Singleton
    fun provideContext(application: Application):Context{
        return application.applicationContext
    }

//    @ProvidesL
//    @Singleton
//    fun provideDatabase(application: Application): AppDatabase =
//        Room.databaseBuilder(application, AppDatabase::class.java, DB_NAME)
//            .allowMainThreadQueries()
//            .fallbackToDestructiveMigration()
//            .build()

    @Provides
    @Singleton
    fun provideHandler(): Handler {
        return Handler()
    }

    @Provides
    @Singleton
    fun provideMessageDataSource(): MessageDataSource {
        return MessageDataSource()
    }

    @Provides
    @Singleton
    fun provideInstagramRepository(application: Application,messageDataSource: MessageDataSource):InstagramRepository{
        return InstagramRepository(application,messageDataSource)
    }


    @Provides
    @Singleton
    fun provideCookieUtils(application: Application): CookieUtils {
        return CookieUtils(application)
    }

    @Provides
    @Singleton
    fun provideThread(): Thread {
        return Thread().apply {
            name = "thread_1"
        }
    }


    @Provides
    fun provideUseCase(application: Application,mInstagramRepository: InstagramRepository,cookieUtils: CookieUtils,gson: Gson): UseCase {
        return UseCase(application,mInstagramRepository,Handler(),gson)
    }

    @Provides
    fun providePlayerManager(application: Application): PlayManager {
        return PlayManager(application.applicationContext)
    }
}
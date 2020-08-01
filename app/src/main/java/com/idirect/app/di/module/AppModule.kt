package com.idirect.app.di.module

import android.app.Application
import android.content.Context
import android.os.Handler
import com.google.gson.Gson
import com.idirect.app.datasource.local.MessageDataSource
import com.idirect.app.datasource.remote.InstagramRemote
import com.idirect.app.repository.InstagramRepository
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

//    @Provides
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
    fun provideInstagramRepository(mInstagramRemote: InstagramRemote,messageDataSource: MessageDataSource):InstagramRepository{
        return InstagramRepository(mInstagramRemote,messageDataSource)
    }


    @Provides
    @Singleton
    fun provideCookieUtils(application: Application): CookieUtils {
        return CookieUtils(application)
    }


    @Provides
    fun provideUseCase(application: Application,mInstagramRepository: InstagramRepository,cookieUtils: CookieUtils,gson: Gson): UseCase {
        return UseCase(application,mInstagramRepository,cookieUtils,Handler(),gson)
    }
}
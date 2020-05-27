package com.sanardev.anemanagement.di.module

import android.app.Application
import android.content.Context
import android.os.Handler
import androidx.room.Room
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
}
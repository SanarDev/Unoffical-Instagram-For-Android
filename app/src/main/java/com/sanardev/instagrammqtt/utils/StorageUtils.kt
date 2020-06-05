package com.sanardev.instagrammqtt.utils

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import java.io.*

class StorageUtils {

    companion object {
        private const val USER_DATA_FILE_NAME = "DrEEct1yHs"
        private const val LAST_LOGIN_DATA_FILE_NAME = "UyUiOOps"

        fun saveLoggedInUserData(context: Context, user: InstagramLoggedUser) {
            saveJsonFile(context, USER_DATA_FILE_NAME,user)
        }

        fun getUserData(context: Context):InstagramLoggedUser?{
            return readFile(context, USER_DATA_FILE_NAME,InstagramLoggedUser::class.java)
        }

        fun saveLastLoginData(
            context: Context,
            instagramLoginPayload: InstagramLoginPayload
        ) {
            saveJsonFile(context,LAST_LOGIN_DATA_FILE_NAME,instagramLoginPayload)
        }

        fun getLastLoginData(context: Context):InstagramLoginPayload?{
            return readFile(context, LAST_LOGIN_DATA_FILE_NAME,InstagramLoginPayload::class.java)
        }

        private fun saveJsonFile(context: Context,filename:String,obj:Any){
            val gson = Gson()
            val json = gson.toJson(obj)
            val logFile = File("${context.filesDir.path}/$filename")
            if (logFile.exists()) {
                try {
                    logFile.delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            logFile.createNewFile()
            try {
//            BufferedWriter for performance, true to set append to file flag
                val buf = BufferedWriter(FileWriter(logFile, true))
                buf.append(json)
                buf.newLine()
                buf.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun <T> readFile(context: Context,filename: String,clazz:Class<T>):T?{
            val file = File("${context.filesDir.path}/$filename")
            if(!file.exists())
                return null
            val inputStream: InputStream = file.inputStream()
            val inputString:String = inputStream.bufferedReader().use { it.readText() }
            if(inputString.isBlank()){
                return null
            }
            val gson = Gson()
            val user = gson.fromJson(inputString,clazz)
            return user
        }
        private fun removeFile(context: Context,filename: String){
            val file = File("${context.filesDir.path}/$filename")
            file.delete()
        }

        fun removeLoggedData(context: Context) {
            removeFile(context, USER_DATA_FILE_NAME)
            removeFile(context, LAST_LOGIN_DATA_FILE_NAME)
        }
    }
}
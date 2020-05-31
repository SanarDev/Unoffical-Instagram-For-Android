package com.sanardev.instagrammqtt.utils

import android.content.Context
import com.google.gson.Gson
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import java.io.*

class StorageUtils {

    companion object {
        const val USER_DATA_FILE_NAME = "DrEEct1yHs"

        fun saveLoggedInUserData(context: Context, user: InstagramLoggedUser) {
            val gson = Gson()
            val json = gson.toJson(user)
            val logFile = File("${context.filesDir.path}/$USER_DATA_FILE_NAME")
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

        fun getUserData(context: Context):InstagramLoggedUser{
            val inputStream: InputStream = File("${context.filesDir.path}/$USER_DATA_FILE_NAME").inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val user = gson.fromJson(inputString,InstagramLoggedUser::class.java)
            return user
        }
    }
}
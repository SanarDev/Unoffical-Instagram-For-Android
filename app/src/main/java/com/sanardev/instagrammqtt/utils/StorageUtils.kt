package com.sanardev.instagrammqtt.utils

import android.app.Application
import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.sanardev.instagrammqtt.datasource.model.Cookie
import com.sanardev.instagrammqtt.datasource.model.FbnsAuth
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import java.io.*

class StorageUtils {

    companion object {
        private const val USER_DATA_FILE_NAME = "DrEEct1yHs"
        private const val LAST_LOGIN_DATA_FILE_NAME = "UyUiOOps"
        private const val COOKIE_BEFORE_LOGIN = "IoPkjTyX"
        private const val FBNS_AUTH = "YuiioQwe"

        fun saveLoggedInUserData(context: Context, user: InstagramLoggedUser) {
            removeFile(context, COOKIE_BEFORE_LOGIN)
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

        fun saveLoginCookie(application: Application,cookie: Cookie){
            saveJsonFile(application, COOKIE_BEFORE_LOGIN,cookie)
        }


        fun saveFbnsAuth(application: Application,fbnsAuth: FbnsAuth) {
            saveJsonFile(application,FBNS_AUTH,fbnsAuth)
        }

        fun getFbnsAuth(application: Application):FbnsAuth{
            val fbns = readFile(application, FBNS_AUTH,FbnsAuth::class.java)
            return fbns ?: FbnsAuth()
        }

        private fun getLoginCookie(application: Application): Cookie? {
            return readFile(application, COOKIE_BEFORE_LOGIN,Cookie::class.java)
        }
        fun removeLoggedData(context: Context) {
            removeFile(context, USER_DATA_FILE_NAME)
            removeFile(context, LAST_LOGIN_DATA_FILE_NAME)
        }

        fun getCookie(application: Application): Cookie? {
            val user = getUserData(application)
            return if(user?.cookie != null){
                user.cookie!!
            }else{
                getLoginCookie(application)
            }
        }

        fun isFileExist(context: Context,audioSrc: String): Boolean {
            val file = File("${context.filesDir.path}/$audioSrc")
            return file.exists()
        }

        fun getFile(context: Context,name: String):File? {
            val file = File("${context.filesDir.path}/$name")
            return if(!file.exists()){
                null
            }else{
                file
            }
        }

        fun saveFile(application: Application, id: String, it: InputStream) {
            val file = File("${application.filesDir.path}/$id")
            if(file.exists())
                file.delete()
            file.createNewFile()
            try {
//            BufferedWriter for performance, true to set append to file flag
                val buf = BufferedWriter(FileWriter(file, true))
                buf.append(it.readBytes().toString())
                buf.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        fun createFileInExternalStorage(application: Application,folderName:String,fileName:String,text:String=""){
            val filesDir: File = application.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
            if (!filesDir.exists()) {
                if (filesDir.mkdirs()) {
                }
            }
            val file = File(filesDir, fileName)
            try {
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        throw IOException("Cant able to create file")
                    }
                }
                val os: OutputStream = FileOutputStream(file)
                val data: ByteArray = text.toByteArray()
                os.write(data)
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }
}
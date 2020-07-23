package com.sanardev.instagrammqtt.utils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.FileOutputStream

class MediaUtils {
    companion object {

        fun getMediaDuration(context: Context?, filePath: String?): Int {
            val uri = Uri.parse(filePath)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val durationStr =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationStr.toInt()
        }

        fun getMediaWidthAndHeight(filePath: String): IntArray {
            val mimeType = getMimeType(filePath) ?: "image/jpeg"
            if (mimeType.contains("image")) {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(filePath)
                val imageHeight = options.outHeight
                val imageWidth = options.outWidth
                return intArrayOf(imageWidth, imageHeight)
            } else {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)
                val width =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                        .toInt()
                val height =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                        .toInt()
                retriever.release()
                return intArrayOf(width, height)
            }
        }

        fun loadImagesfromSDCard2(context: Context): ArrayList<String> {
            val uri: Uri = android.provider.MediaStore.Files.getContentUri("external")
            val cursor: Cursor?
            val column_index_data: Int
            val column_index_folder_name: Int
            val listOfAllImages = ArrayList<String>()
            var absolutePathOfImage: String? = null

            val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

            val orderBy = MediaStore.Files.FileColumns.DATE_ADDED;
            val projection =
                arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            cursor = context.contentResolver.query(uri, projection, selection, null, orderBy)

            column_index_data = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            column_index_folder_name = cursor!!
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor!!.moveToNext()) {
                absolutePathOfImage = cursor!!.getString(column_index_data)
                listOfAllImages.add(absolutePathOfImage)
            }
            return listOfAllImages
        }

        fun getMimeType(filePath: String): String? {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            return type
        }

        fun convertImageFormatToJpeg(inputPath:String,outputPath:String){
            try {
                val bmp = BitmapFactory.decodeFile(inputPath)
                val out = FileOutputStream(outputPath)
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out) //100-best quality
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
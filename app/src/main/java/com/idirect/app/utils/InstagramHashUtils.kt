@file:JvmName("InstagramHashUtils")
package com.idirect.app.utils

import com.idirect.app.constants.InstagramConstants
import com.idirect.app.extentions.toHexString
import java.lang.StringBuilder
import java.net.URLEncoder
import java.security.Key
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class InstagramHashUtils {
    companion object{
        fun generateHash(key: String, string: String): String? {
            val `object` = SecretKeySpec(key.toByteArray(), "HmacSHA256")
            try {
                val mac = Mac.getInstance("HmacSHA256")
                mac.init(`object` as Key)
                val byteArray = mac.doFinal(string.toByteArray(charset("UTF-8")))
                return byteArray.toHexString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Generate signed payload
         * @param payload Payload
         * @return Signed string
         */
        fun generateSignature(payload: String): String {
            val parsedData = URLEncoder.encode(payload, "UTF-8")
            val signedBody = generateHash(InstagramConstants.API_KEY, payload)
            return StringBuilder()
                .append("ig_sig_key_version=")
                .append(InstagramConstants.API_KEY_VERSION)
                .append("&signed_body=")
                .append(signedBody)
                .append(".")
                .append(parsedData)
                .toString()
        }

        fun getClientContext(): String {
            var rnd = Random();
            var str = "";
            // 6600286272511816379
            str += rnd.nextInt(9);
            str += rnd.nextInt(9);
            str += rnd.nextInt(9);
            //str += Rnd.Next(11, 99);
            str += (Math.random() * (9999 - 1000 + 1) + 1000).toInt()
            str += (Math.random() * (99999 - 11111 + 1) + 11111).toInt()

            str += (Math.random() * (6789 - 2222 + 1) + 2222).toInt()
            return "668${str}";
        }

        @JvmStatic
        fun generatePacketID(): Int {
            return Random().nextInt(65535)
        }

        @JvmStatic
        fun getUploadId(story:Boolean = false):String{
            var r = if(story) "18" else "37"
            for(i in 0..15){
                r += (Math.random() * (0 - 9 + 1) + 0).toInt().toString()
            }
            return r
        }
    }
}
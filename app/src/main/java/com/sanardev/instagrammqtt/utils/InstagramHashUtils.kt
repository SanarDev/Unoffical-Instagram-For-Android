package com.sanardev.instagrammqtt.utils

import com.sanardev.instagrammqtt.constants.InstagramConstants
import run.tripa.android.extensions.toHexString
import java.lang.StringBuilder
import java.net.URLEncoder
import java.security.Key
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
    }
}
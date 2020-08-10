@file:JvmName("InstagramHashUtils")
package com.idirect.app.utils

import com.idirect.app.constants.InstagramConstants
import com.idirect.app.extentions.toHexString
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
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

        fun encryptPassword(
            password: String,
            enc_id: String?,
            enc_pub_key: String?
        ): String? {
            val rand_key = ByteArray(32)
            val iv = ByteArray(12)
            val sran = SecureRandom()
            sran.nextBytes(rand_key)
            sran.nextBytes(iv)
            val time = (System.currentTimeMillis() / 1000).toString()

            // Encrypt random key
            val decoded_pub_key = String(
                Base64.decodeBase64(enc_pub_key),
                StandardCharsets.UTF_8
            ).replace("-----BEGIN PUBLIC KEY-----", "").replace("\n-----END PUBLIC KEY-----", "")
            val rsa_cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING")
            rsa_cipher.init(
                Cipher.ENCRYPT_MODE,
                KeyFactory.getInstance("RSA")
                    .generatePublic(X509EncodedKeySpec(Base64.decodeBase64(decoded_pub_key)))
            )
            val rand_key_encrypted: ByteArray = rsa_cipher.doFinal(rand_key)

            // Encrypt password
            val aes_gcm_cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
            aes_gcm_cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(rand_key, "AES"),
                GCMParameterSpec(128, iv)
            )
            aes_gcm_cipher.updateAAD(time.toByteArray())
            val password_encrypted: ByteArray =
                aes_gcm_cipher.doFinal(password.toByteArray())

            // Write to final byte array
            val out = ByteArrayOutputStream()
            out.write(ByteBuffer.allocate(Int.SIZE_BYTES).putInt(1).array())
            out.write(ByteBuffer.allocate(Int.SIZE_BYTES).putInt(enc_id!!.toInt()).array())
            out.write(iv)
            out.write(
                ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
                    .putChar(rand_key_encrypted.size.toChar()).array()
            )
            out.write(rand_key_encrypted)
            out.write(
                Arrays.copyOfRange(
                    password_encrypted,
                    password_encrypted.size - 16,
                    password_encrypted.size
                )
            )
            out.write(Arrays.copyOfRange(password_encrypted, 0, password_encrypted.size - 16))
            return java.lang.String.format(
                "#PWD_INSTAGRAM:%s:%s:%s",
                "4",
                time,
                Base64.encodeBase64String(out.toByteArray())
            )
        }
    }
}
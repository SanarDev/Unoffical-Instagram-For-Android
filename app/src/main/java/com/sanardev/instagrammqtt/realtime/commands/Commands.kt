package com.sanardev.instagrammqtt.realtime.commands

import android.util.Log
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.ParsedMessage
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.protocol.TField
import org.apache.thrift.protocol.TProtocolUtil
import org.apache.thrift.protocol.TType
import org.apache.thrift.transport.TMemoryBuffer

class Commands {
    companion object {
        fun parseData(json: ByteArray): ParsedMessage {
            val a = TMemoryBuffer(json.size)
            a.write(json)
            val iprot = TCompactProtocol(a)
            var topic: String = ""
            var payload: String = ""
            var field: TField
            while (true) {
                field = iprot.readFieldBegin()
                if (field.type == TType.STOP) {
                    break
                }
                when (field.id.toInt()) {
                    1 -> {
                        if (field.type == TType.STRING) {
                            topic = iprot.readString()
                        } else {
                            TProtocolUtil.skip(iprot, field.type)
                        }
                    }
                    2 -> {
                        if (field.type == TType.STRING) {
                            payload = iprot.readString()
                        } else {
                            TProtocolUtil.skip(iprot, field.type)
                        }
                    }
                    else -> {
                        TProtocolUtil.skip(iprot, field.type)
                    }
                }
                iprot.readFieldEnd()
            }

            return ParsedMessage(topic, payload)
        }
    }
}
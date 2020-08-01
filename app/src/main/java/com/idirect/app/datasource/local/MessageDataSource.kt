package com.idirect.app.datasource.local

import com.idirect.app.datasource.model.Message

class MessageDataSource {

    // قبل از استفاده از این کلاس باید به این توجه شود که در صورت حذف پیام از طریق RealTime اعلام میگردد یا ن
    // key : threadId | value : Message list
    private val localMessages  = HashMap<String,MutableList<Message>>()

    fun getMessages(threadId: String,cursor:String?=null,limit:Int = 0):List<Message>?{
        val msgs = localMessages[threadId]
        if(msgs == null){
            return null
        }else{
            if(cursor == null){
                return msgs.subList(0,limit)
            }else{
                for(i in msgs.indices){
                    if(msgs[i].itemId == cursor){
                        return msgs.subList(i + 1,i + 1 + limit)
                    }
                }
                return msgs.subList(0,limit)
            }
        }
    }
    fun saveMessages(threadId:String,messages:List<Message>){
        if(localMessages[threadId] == null){
            localMessages[threadId] = emptyList<Message>().toMutableList()
        }

        val msg = localMessages[threadId]!!
        if(msg.isEmpty()){
            msg.addAll(messages)
            return
        }
        for (message in messages){
            var isMsgExist = false
            for(localMessage in msg){
                if(localMessage.itemId == message.itemId){
                    isMsgExist = true
                    break
                }
                if(!isMsgExist){
                    msg.add(message)
                }
            }
        }
    }
    /*
    چجوری پیام هارو سیو کنم؟
    خب من انتظار دارم که یک threadId به همراه حالا cursor و سپس بتونم در صورت وجود پیام ها در لوکال اونارو از همین جا بگیرم
    بنابراین:
    پس یک هش مپ اول بیام بسازم که کلیدش threadId باشه و مقدارش هم برابر با Message
     */

}
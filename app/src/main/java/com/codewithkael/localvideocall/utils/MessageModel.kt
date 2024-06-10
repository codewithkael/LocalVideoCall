package com.codewithkael.localvideocall.utils

enum class MessageModelType{
     OFFER,ANSWER,ICE
}

data class MessageModel(
     val type: MessageModelType,
     val data:Any?=null
)

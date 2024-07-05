package com.codewithkael.localvideocall.remote.socket.client

import com.codewithkael.localvideocall.utils.MessageModel

interface SocketClientListener {
    fun onSocketClientOpened()
    fun onSocketClientMessage(message: MessageModel)
}
package com.codewithkael.localvideocall.remote.socket.server

import com.codewithkael.localvideocall.utils.MessageModel

interface SocketClientListener {
    fun onSocketClientOpened()
    fun onSocketClientMessage(message: MessageModel)
}
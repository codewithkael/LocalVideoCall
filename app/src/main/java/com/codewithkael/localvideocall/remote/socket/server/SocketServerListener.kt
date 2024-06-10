package com.codewithkael.localvideocall.remote.socket.server

import com.codewithkael.localvideocall.utils.MessageModel


interface SocketServerListener {
    fun onSocketServerNewMessage(message: MessageModel)
    fun onStartServer(port:Int)
}

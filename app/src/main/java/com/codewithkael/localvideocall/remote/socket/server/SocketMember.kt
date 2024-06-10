package com.codewithkael.localvideocall.remote.socket.server

import org.java_websocket.WebSocket


data class SocketMember(
    val name:String,
    val connection: WebSocket
)

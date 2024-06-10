package com.codewithkael.localvideocall.remote.socket.server

import android.util.Log
import com.codewithkael.localvideocall.utils.MessageModel
import com.google.gson.Gson
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import javax.inject.Inject

class SocketServer @Inject constructor(
    private val gson: Gson,
) {

    private var socketserver: WebSocketServer? = null
    private var memberToCall: SocketMember? = null

    private var socketServerPort = 3013

    private val TAG = "SocketServer"
    fun init(
        listener: SocketServerListener,
    ) {
        Log.d(TAG, "init: called")
        if (socketserver == null) {
            socketserver = object : WebSocketServer(InetSocketAddress(socketServerPort)) {
                override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {

                }

                override fun onClose(
                    conn: WebSocket?,
                    code: Int,
                    reason: String?,
                    remote: Boolean
                ) {

                }

                override fun onMessage(conn: WebSocket?, message: String?) {
                    Log.d(TAG, "onMessage: $message")
                    runCatching {
                        gson.fromJson(message, MessageModel::class.java)
                    }.onSuccess { data ->
                        listener.onSocketServerNewMessage(data)
                    }
                }

                override fun onError(conn: WebSocket?, ex: Exception?) {
                    Log.d(TAG, "onError1: ${ex?.message}")
                    if (ex?.message == "Address already in use") {
                        socketServerPort++
                        onDestroy()
                        init(listener)
                    }
                    ex?.printStackTrace()
                }

                override fun onStart() {
                    listener.onStartServer(socketServerPort)
                    Log.d(TAG, "onStart: ")
                }
            }.apply { start() }
        }
    }


    fun sendDataToClient(dataModel: MessageModel) {
        runCatching {
            memberToCall?.let {
                val jsonedModel = gson.toJson(dataModel)
                it.connection.send(
                    jsonedModel
                )
            }
        }
    }

    fun onDestroy() = runCatching {
        socketserver?.stop()
        socketserver = null
    }
}
package com.codewithkael.localvideocall.remote.socket.client

import com.codewithkael.localvideocall.remote.socket.server.SocketClientListener
import com.codewithkael.localvideocall.utils.MessageModel
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject


class SocketClient @Inject constructor(
    private val gson: Gson,
) {
    private var webSocket: WebSocketClient? = null

    fun init(socketAddress: String, listener: SocketClientListener, onError: () -> Unit) {
        webSocket = object : WebSocketClient(URI("ws://$socketAddress")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                listener.onSocketClientOpened()
            }

            override fun onMessage(message: String?) {
                runCatching {
                    gson.fromJson(message.toString(), MessageModel::class.java)
                }.onSuccess {
                    listener.onSocketClientMessage(it)
                }
            }


            override fun onClose(code: Int, reason: String?, remote: Boolean) {
            }

            override fun onError(ex: Exception?) {
                onError.invoke()
            }

        }
        webSocket?.connect()

    }

    fun sendDataToHost(message: MessageModel) {
        try {
            webSocket?.send(gson.toJson(message))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onDestroy() {
        runCatching {
            webSocket?.close()
        }
    }
}
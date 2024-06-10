package com.codewithkael.localvideocall.remote.socket.client

import android.util.Log
import com.codewithkael.localvideocall.remote.socket.server.SocketClientListener
import com.codewithkael.localvideocall.utils.MessageModel
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketClient @Inject constructor(
    private val gson:Gson,
) {
    private var webSocket: WebSocketClient? = null
    private val TAG = "SocketRepository"

    fun init(socketAddress:String, listener: SocketClientListener,onError:()->Unit) {
        webSocket = object : WebSocketClient(URI("ws://$socketAddress")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                listener.onSocketClientOpened()
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "onMessage: $message")
                try {
                    listener.onSocketClientMessage(gson.fromJson(message, MessageModel::class.java))

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose: $reason")
            }

            override fun onError(ex: Exception?) {
                Log.d(TAG, "onError: $ex")
                onError.invoke()
            }

        }
        webSocket?.connect()

    }

    fun sendDataToHost(message: MessageModel) {
        try {
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.d(TAG, "sendMessageToSocket: $e")
        }
    }
}
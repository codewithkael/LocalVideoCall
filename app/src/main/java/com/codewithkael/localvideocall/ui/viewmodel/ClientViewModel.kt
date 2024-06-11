package com.codewithkael.localvideocall.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import com.codewithkael.localvideocall.remote.socket.client.SocketClient
import com.codewithkael.localvideocall.remote.socket.server.SocketClientListener
import com.codewithkael.localvideocall.utils.MessageModel
import com.codewithkael.localvideocall.utils.MessageModelType.ANSWER
import com.codewithkael.localvideocall.utils.MessageModelType.ICE
import com.codewithkael.localvideocall.webrtc.PeerConnectionObserver
import com.codewithkael.localvideocall.webrtc.RTCClient
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val application: Application,
    private val socketClient: SocketClient,
    private val gson: Gson
) : ViewModel(), SocketClientListener {

    private val TAG = "SocketRepository"

    //webrtc variables
    @SuppressLint("StaticFieldLeak")
    private var remoteView: SurfaceViewRenderer? = null
    private val rtcClient: RTCClient by lazy {
        RTCClient(application, object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                rtcClient.addIceCandidate(p0)
                socketClient.sendDataToHost(MessageModel(ICE, gson.toJson(p0)))
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                remoteView?.let { remote ->
                    p0?.let { mediaStream ->
                        mediaStream.videoTracks[0]?.addSink(remote)
                    }
                }

            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                Log.d(TAG, "onConnectionChange: $newState")
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                super.onIceConnectionChange(p0)
                Log.d(TAG, "onConnectionChange ice state: $p0")

            }

        }) {
            socketClient.sendDataToHost(it)
        }
    }

    fun init(serverAddress: String, onError: () -> Unit) {
        startSocketClient(serverAddress, onError)
    }


    private fun startSocketClient(serverAddress: String, onError: () -> Unit) {
        socketClient.init(serverAddress, this@ClientViewModel) {
            onError.invoke()
        }
    }

    private fun handlerAnswerReceived(message: MessageModel) {
        val remoteSDP = SessionDescription(SessionDescription.Type.ANSWER, message.data.toString())
        rtcClient.onRemoteSessionReceived(remoteSDP)
    }

    private fun handleIceReceived(message: MessageModel) {
        runCatching {
            gson.fromJson(message.toString(), IceCandidate::class.java)
        }.onSuccess {
            rtcClient.addIceCandidate(it)
        }
    }


    fun startLocalStream(view: SurfaceViewRenderer) {
        rtcClient.startLocalVideo(view)
    }

    fun prepareRemoteSurfaceView(view: SurfaceViewRenderer) {
        this.remoteView = view
        rtcClient.initializeRemoteSurfaceView(view)
    }

    override fun onCleared() {
        super.onCleared()
        remoteView?.release()
        remoteView = null
        rtcClient.onDestroy()
        socketClient.onDestroy()
    }

    override fun onSocketClientOpened() {
        rtcClient.call()
    }

    override fun onSocketClientMessage(message: MessageModel) {
        when (message.type) {
            ANSWER -> handlerAnswerReceived(message)
            ICE -> handleIceReceived(message)
            else -> {}
        }
    }

}
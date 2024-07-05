package com.codewithkael.localvideocall.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithkael.localvideocall.remote.socket.client.SocketClient
import com.codewithkael.localvideocall.remote.socket.client.SocketClientListener
import com.codewithkael.localvideocall.utils.MessageModel
import com.codewithkael.localvideocall.utils.MessageModelType.ANSWER
import com.codewithkael.localvideocall.utils.MessageModelType.ICE
import com.codewithkael.localvideocall.webrtc.PeerConnectionObserver
import com.codewithkael.localvideocall.webrtc.RTCAudioManager
import com.codewithkael.localvideocall.webrtc.RTCClient
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

    val callDisconnected: MutableStateFlow<Boolean> = MutableStateFlow(false)


    //webrtc variables
    @SuppressLint("StaticFieldLeak")
    private var remoteView: SurfaceViewRenderer? = null
    private val rtcAudioManager by lazy { RTCAudioManager.create(application) }
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
                if (newState == PeerConnection.PeerConnectionState.DISCONNECTED ||
                    newState == PeerConnection.PeerConnectionState.CLOSED
                ) {
                    viewModelScope.launch {
                        callDisconnected.emit(true)
                    }
                }
            }
        }) {
            socketClient.sendDataToHost(it)
        }
    }

    fun init(serverAddress: String, onError: () -> Unit) {
        startSocketClient(serverAddress, onError)
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
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
            gson.fromJson(message.data.toString(), IceCandidate::class.java)


        }.onSuccess {
            rtcClient.addIceCandidate(it)
        }.onFailure {
            it.printStackTrace()
        }
    }


    fun startLocalStream(view: SurfaceViewRenderer) {
        rtcClient.startLocalVideo(view)
    }

    fun prepareRemoteSurfaceView(view: SurfaceViewRenderer) {
        this.remoteView = view
        rtcClient.initializeSurfaceView(view)
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

    fun switchCamera() {
        rtcClient.switchCamera()
    }

    fun toggleAudio(muted: Boolean) {
        rtcClient.toggleAudio(muted)
    }

    fun toggleVideo(muted: Boolean) {
        rtcClient.toggleVideo(muted)
    }

    fun endCall() {
        rtcClient.endCall()
        viewModelScope.launch {
            callDisconnected.emit(true)
        }
    }

    fun toggleOutputAudio(isSpeakerOn: Boolean) {
        if (isSpeakerOn) {
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            rtcAudioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        } else {
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
            rtcAudioManager.selectAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
        }
    }

}
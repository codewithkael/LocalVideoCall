package com.codewithkael.localvideocall.webrtc

import android.app.Application
import com.codewithkael.localvideocall.utils.MessageModel
import com.codewithkael.localvideocall.utils.MessageModelType.ANSWER
import com.codewithkael.localvideocall.utils.MessageModelType.OFFER
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class RTCClient(
    private val application: Application,
    private val observer: PeerConnection.Observer,
    private val sendMessageToSocket: (model: MessageModel) -> Unit
) {

    private val iceServer = listOf<PeerConnection.IceServer>()
    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }

    init {
        initPeerConnectionFactory(application)
    }

    private val eglContext = EglBase.create()
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }

    private val peerConnection by lazy { createPeerConnection(observer) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private var videoCapturer: CameraVideoCapturer? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null


    private fun initPeerConnectionFactory(application: Application) {
        val peerConnectionOption = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()

        PeerConnectionFactory.initialize(peerConnectionOption)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglContext.eglBaseContext,
                    true,
                    true
                )
            )
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglContext.eglBaseContext))
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            }).createPeerConnectionFactory()
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }

    private fun initializeSurfaceView(surface: SurfaceViewRenderer) {
        surface.run {
            setEnableHardwareScaler(true)
            setMirror(true)
            init(eglContext.eglBaseContext, null)
        }
    }

    fun initializeRemoteSurfaceView(surface: SurfaceViewRenderer) {
        surface.run {
            setEnableHardwareScaler(true)
            setMirror(true)
            init(eglContext.eglBaseContext, null)
        }
    }

    fun startLocalVideo(surface: SurfaceViewRenderer) {
        initializeSurfaceView(surface)
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, eglContext.eglBaseContext)
        videoCapturer = getVideoCapturer(application)
        videoCapturer?.initialize(
            surfaceTextureHelper,
            surface.context, localVideoSource.capturerObserver
        )
        videoCapturer?.startCapture(1080, 720, 30)
        localVideoTrack = peerConnectionFactory.createVideoTrack("local_track", localVideoSource)
        localVideoTrack?.addSink(surface)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource)
        val localStream = peerConnectionFactory.createLocalMediaStream("local_stream")
        localStream.addTrack(localAudioTrack)
        localStream.addTrack(localVideoTrack)
        peerConnection?.addStream(localStream)
    }

    private fun getVideoCapturer(application: Application): CameraVideoCapturer {
        return Camera2Enumerator(application).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }
    }

    fun call() {
        peerConnection?.createOffer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        sendMessageToSocket(
                            MessageModel(OFFER, desc?.description)
                        )
                    }
                }, desc)
            }
        }, mediaConstraints)
    }

    fun answer() {
        peerConnection?.createAnswer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        sendMessageToSocket(
                            MessageModel(
                                ANSWER, desc?.description
                            )
                        )
                    }

                }, desc)
            }
        }, mediaConstraints)
    }

    fun onRemoteSessionReceived(session: SessionDescription) {
        peerConnection?.setRemoteDescription(MySdpObserver(), session)
    }

    fun addIceCandidate(p0: IceCandidate?) {
        peerConnection?.addIceCandidate(p0)
    }

    fun onDestroy() {
        runCatching {
            localVideoTrack?.dispose()
            localAudioSource?.dispose()
            videoCapturer?.dispose()
            peerConnection?.close()
        }
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }


    fun toggleAudio(mute: Boolean) {
        localAudioTrack?.setEnabled(mute)
    }

    fun toggleVideo(cameraPause: Boolean) {
        localVideoTrack?.setEnabled(cameraPause)
    }

    fun endCall() {
        peerConnection?.close()
    }
}
package com.codewithkael.localvideocall.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithkael.localvideocall.ui.components.ControlButtonsLayout
import com.codewithkael.localvideocall.ui.components.SurfaceViewRendererComposable
import com.codewithkael.localvideocall.ui.viewmodel.HostViewModel
import com.codewithkael.localvideocall.utils.Constants

@Composable
fun HostScreen(navController: NavController) {

    val hostViewModel: HostViewModel = hiltViewModel()

    val hostAddress = hostViewModel.hostAddressState.collectAsState()
    LaunchedEffect(key1 = Unit) {
        hostViewModel.init {

        }
    }

    val callConnectedState = hostViewModel.callDisconnected.collectAsState()
    if (callConnectedState.value) {
        navController.navigate(Constants.MAIN_SCREEN)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Remote surface
        SurfaceViewRendererComposable(
            modifier = Modifier
                .fillMaxSize()
        ) {
            hostViewModel.prepareRemoteSurfaceView(it)
        }

        Text(
            text = hostAddress.value ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(10.dp)
                .align(Alignment.TopCenter)
                .shadow(8.dp, shape = RoundedCornerShape(4.dp)),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        SurfaceViewRendererComposable(
            modifier = Modifier
                .padding(top = 50.dp, end = 10.dp)
                .size(90.dp,120.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(22.dp))
                .shadow(8.dp, shape = RoundedCornerShape(12.dp))
        ) {
            hostViewModel.startLocalStream(it)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 0f,
                        endY = 150f
                    )
                )
        ) {
            ControlButtonsLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(8.dp, shape = RoundedCornerShape(12.dp)),
                onAudioButtonClicked = hostViewModel::toggleAudio,
                onCameraButtonClicked = hostViewModel::toggleVideo,
                onSpeakerModeButtonClicked = hostViewModel::toggleOutputAudio,
                onEndCallClicked = hostViewModel::endCall,
                onSwitchCameraClicked = hostViewModel::switchCamera
            )
        }
    }
}

package com.codewithkael.localvideocall.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithkael.localvideocall.ui.components.ControlButtonsLayout
import com.codewithkael.localvideocall.ui.components.SurfaceViewRendererComposable
import com.codewithkael.localvideocall.ui.viewmodel.ClientViewModel
import com.codewithkael.localvideocall.utils.Constants.MAIN_SCREEN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ClientScreen(navController: NavController, serverAddress: String?) {
    val clientViewModel: ClientViewModel = hiltViewModel()
    val context = LocalContext.current

    val callConnectedState = clientViewModel.callDisconnected.collectAsState()
    if (callConnectedState.value){
        navController.navigate(MAIN_SCREEN)
    }

    LaunchedEffect(key1 = Unit) {
        clientViewModel.init(serverAddress!!) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Failed connecting to host", Toast.LENGTH_SHORT).show()
                navController.navigate(MAIN_SCREEN)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Remote surface
        SurfaceViewRendererComposable(
            modifier = Modifier
                .fillMaxSize()
        ) {
            clientViewModel.prepareRemoteSurfaceView(it)
        }

        SurfaceViewRendererComposable(
            modifier = Modifier
                .padding(top = 20.dp, end = 10.dp)
                .size(90.dp,120.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(22.dp))
                .shadow(8.dp, shape = RoundedCornerShape(12.dp))
        ) {
            clientViewModel.startLocalStream(it)
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
                onAudioButtonClicked = clientViewModel::toggleAudio,
                onCameraButtonClicked = clientViewModel::toggleVideo,
                onSpeakerModeButtonClicked = clientViewModel::toggleOutputAudio,
                onEndCallClicked = clientViewModel::endCall,
                onSwitchCameraClicked = clientViewModel::switchCamera
            )
        }
    }
}
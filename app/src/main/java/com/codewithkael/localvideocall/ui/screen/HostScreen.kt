package com.codewithkael.localvideocall.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithkael.localvideocall.ui.components.SurfaceViewRendererComposable
import com.codewithkael.localvideocall.ui.viewmodel.HostViewModel

@Composable
fun HostScreen(navController: NavController) {

    val hostViewModel: HostViewModel = hiltViewModel()

    val hostAddress = hostViewModel.hostAddressState.collectAsState()
    LaunchedEffect(key1 = Unit) {
        hostViewModel.init {

        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = hostAddress.value ?: "", modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .weight(1f), textAlign = TextAlign.Center
            )

            //local surface
            SurfaceViewRendererComposable(modifier = Modifier
                .fillMaxWidth()
                .weight(8f)) {
                hostViewModel.startLocalStream(it)
            }

            //remote surface
            SurfaceViewRendererComposable(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .weight(8f)
            ) {
                hostViewModel.prepareRemoteSurfaceView(it)
            }
        }
    }
}
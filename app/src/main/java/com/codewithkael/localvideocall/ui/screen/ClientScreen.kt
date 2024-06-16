package com.codewithkael.localvideocall.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

    LaunchedEffect(key1 = Unit) {
        clientViewModel.init(serverAddress!!) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Failed connecting to host", Toast.LENGTH_SHORT).show()
                navController.navigate(MAIN_SCREEN)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            //local surface
            SurfaceViewRendererComposable(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(8f)
            ) {
                clientViewModel.startLocalStream(it)
            }

            //remote surface
            SurfaceViewRendererComposable(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .weight(8f)
            ) {
                clientViewModel.prepareRemoteSurfaceView(it)
            }
        }
    }
}
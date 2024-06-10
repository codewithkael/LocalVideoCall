package com.codewithkael.localvideocall.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.codewithkael.localvideocall.utils.Constants.HOST_SCREEN
import com.codewithkael.localvideocall.utils.Constants.clientScreen

@Composable
fun MainScreen(navController: NavController) {

    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // All permissions are granted
        if (!permissions.all { it.value }) {
            Toast.makeText(
                context,
                "Camera And Microphone permissions are required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(key1 = Unit) {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        )
    }

    Column(
        Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Join As", modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(50.dp), textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                navController.navigate(HOST_SCREEN)
            },
            modifier = Modifier
                .width(200.dp)
                .height(45.dp)
        ) {
            Text(text = "Host", Modifier.fillMaxSize(), textAlign = TextAlign.Center)
        }

        Text(
            text = "Or", modifier = Modifier
                .fillMaxWidth()
                .padding(top = 70.dp)
                .height(50.dp), textAlign = TextAlign.Center
        )
        var addressState by remember { mutableStateOf("") }
        TextField(modifier = Modifier.padding(top = 5.dp), value = addressState, onValueChange = {
            addressState = it
        }, placeholder = {
            Text(text = "Enter Host Address")
        })

        Button(
            onClick = {
                if (addressState.isEmpty()) {
                    Toast.makeText(context, "Enter Server Address", Toast.LENGTH_SHORT).show()
                } else {
                    navController.navigate(clientScreen(addressState))
                }
            },
            modifier = Modifier
                .width(200.dp)
                .padding(top = 10.dp)
                .height(45.dp)
        ) {
            Text(text = "Client", Modifier.fillMaxSize(), textAlign = TextAlign.Center)
        }
    }

}
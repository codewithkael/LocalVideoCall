package com.codewithkael.localvideocall.utils

object Constants {
    const val MAIN_SCREEN = "MainScreen"
    const val HOST_SCREEN = "HostScreen"
    fun clientScreen(serverAddress: String) = "ClientScreen/$serverAddress"

}
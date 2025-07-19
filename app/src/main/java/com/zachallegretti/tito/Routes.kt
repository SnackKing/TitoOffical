package com.zachallegretti.tito

// Define the different states/screens for your app
object Routes {
    const val MAIN_MENU = "main_menu"
    const val SOUNDBOARD = "soundboard"
    const val EAT_LIKE_TITO = "eat_like_tito"
    const val GALLERY = "gallery"
    const val POINTS = "points"
    const val AUTH = "auth"
    const val CREATE_BET = "create_bet/{username}" // New route for creating a bet, now expects username
    const val BET_LOBBY = "bet_lobby"

    fun betLobbyRoute(lobbyId: String) = "bet_lobby/$lobbyId"
    fun createBetRoute(username: String) = "create_bet/$username"


}
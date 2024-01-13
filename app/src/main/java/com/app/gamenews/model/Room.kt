package com.app.gamenews.model

data class Room(
    val roomId: String = "",
    val time : Long = 0,
    val adminId : String = "",
    val maxUsers : Int = 0,
    val currentUsers : Int = 0,
    val title : String = "",
    val game : String = "",
    val discordUrl : String = "",
    val roomImage : String = ""
) {
}
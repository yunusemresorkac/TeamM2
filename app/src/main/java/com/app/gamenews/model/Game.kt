package com.app.gamenews.model

data class Game(
    val time : String = "",
    val title : String = "",
    val imageUrls : MutableList<String> = mutableListOf(),
    val link : String = "",
    val countOfLikes : Int = 0,
    val countOfComments : Int = 0
) {
}
package com.app.gamenews.model

data class Post(
    val userId : String = "",
    val postId : String = "",
    val time : Long = 0,
    val title : String = "",
    var imageUrls : MutableList<String> = mutableListOf(),
    val countOfLikes : Int = 0,
    val countOfComments : Int = 0
    ) {
}
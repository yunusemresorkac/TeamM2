package com.app.gamenews.model

data class Comment(
    val postId : String = "",
    val commentId : String = "",
    val time : Long = 0,
    val comment : String = "",
    val userId : String = ""
) {
}
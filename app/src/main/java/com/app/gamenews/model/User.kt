package com.app.gamenews.model

data class User(
    val userId : String = "",
    val registerDate : Long = 0,
    val username : String = "",
    val email : String = "",
    val coin : Double = 0.0,
    val accountType : Int = 0,
    val status : Int = 0,
    val profileImage : String = "",
    val followers : Int = 0,
    val following : Int = 0,
    val posts: Int = 0
): Comparable<User> {
    override fun compareTo(other: User): Int {
        return username.compareTo(other.username)
    }
}

package com.poisonedyouth.richdomain

interface UserRepository {

    fun isUsernameAlreadyUsed(username: String): Boolean

    fun save(user: User): User

    fun getBy(username: String): User

    fun update(user: User): User
}
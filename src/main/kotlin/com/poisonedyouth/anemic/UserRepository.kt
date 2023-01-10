package com.poisonedyouth.anemic

interface UserRepository {

    fun isUsernameAlreadyUsed(username: String): Boolean

    fun save(user: User): User

    fun findBy(username: String): User?

    fun update(user: User): User
}
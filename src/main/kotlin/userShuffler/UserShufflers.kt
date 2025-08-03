package org.morningmeadow.userShuffler

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import dev.kord.core.entity.effectiveName
import org.morningmeadow.Bot

object UserShufflers {
    const val USER_LIMIT = 16
    private var shufflers: MutableMap<Pair<Snowflake, Snowflake>, UserShuffler> = mutableMapOf()

     fun getUsers(key: Pair<Snowflake, Snowflake>): List<org.morningmeadow.userShuffler.User>? {
        val shuffler = shufflers[key]
        if (shuffler == null) return null
        return shuffler.users.toList()
    }

    fun shuffleUsers(key: Pair<Snowflake, Snowflake>) {
        val shuffler = shufflers[key]
        if (shuffler == null) throw IndexOutOfBoundsException()
        shuffler.users.shuffle()
    }

    fun createEmptyShuffler(key: Pair<Snowflake, Snowflake>) {
        shufflers[key] = UserShuffler(mutableListOf())
    }

    fun exists(key: Pair<Snowflake, Snowflake>): Boolean {
        return key in shufflers
    }

    fun remove(key: Pair<Snowflake, Snowflake>) {
        shufflers.remove(key)
    }

    suspend fun addUser(key: Pair<Snowflake, Snowflake>, userId: Snowflake) {
        val user = Bot.kord.getUser(userId)!!
        shufflers[key]!!.users.add(User(userId, user.effectiveName))
    }

    fun removeUser(key: Pair<Snowflake, Snowflake>, userId: Snowflake) {
        shufflers[key]!!.users.removeAll { user -> user.id == userId }
    }

    fun shufflerSize(key: Pair<Snowflake, Snowflake>): Int {
        return shufflers[key]!!.users.size
    }
}
package org.morningmeadow.userShuffler

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.morningmeadow.Bot

object UserShufflers {
    const val USER_LIMIT = 10
    private var shufflers: MutableMap<Pair<Snowflake, Snowflake>, UserShuffler> = mutableMapOf()

     fun getUsers(key: Pair<Snowflake, Snowflake>): Flow<User>? {
        val shuffler = shufflers[key]
        if (shuffler == null) return null
        return flow {
            shuffler.users.forEach {
                userId ->
                val user = Bot.kord.getUser(userId)
                if (user != null) emit(user)
            }
        }
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

    fun addUser(key: Pair<Snowflake, Snowflake>, userId: Snowflake) {
        shufflers[key]!!.users.add(userId)
    }

    fun removeUser(key: Pair<Snowflake, Snowflake>, userId: Snowflake) {
        shufflers[key]!!.users.remove(userId)
    }

    fun shufflerSize(key: Pair<Snowflake, Snowflake>): Int {
        return shufflers[key]!!.users.size
    }
}
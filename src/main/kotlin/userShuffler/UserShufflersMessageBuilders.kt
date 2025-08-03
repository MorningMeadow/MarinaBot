package org.morningmeadow.userShuffler

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import dev.kord.core.entity.effectiveName
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.morningmeadow.userShuffler.UserShufflers.USER_LIMIT
import kotlin.random.Random

object UserShufflersMessageBuilders {
    private const val SIMULATED_DICE_SIZE: Int = 100

    suspend fun home(builder: MessageBuilder, userFlow: Flow<User>, simulateDice: Boolean = false) {
        val users = userFlow.toList()
        val userOL = generateUserOrderedList(users, simulateDice)
        var content = userOL
        if (users.size == 1) {
            content += "\nYou need at least two users to shuffle the list."
        }
        if (users.size >= USER_LIMIT) {
            content += "\nYou've reached the maximum user limit of $USER_LIMIT."
        }

        builder.content = content
        builder.actionRow {
            interactionButton(ButtonStyle.Primary, "add_user") {
                label = "Add user(s)"
                disabled = users.size >= USER_LIMIT
            }
            interactionButton(ButtonStyle.Primary, "remove_user") {
                label = "Remove user(s)"
                disabled = users.isEmpty()
            }
            interactionButton(ButtonStyle.Success, "shuffle") {
                label = "Shuffle"
                disabled = users.size < 2
            }
            interactionButton(ButtonStyle.Danger, "cancel") {
                label = "Cancel"
            }
        }
    }

    suspend fun addUser(builder: MessageBuilder, userFlow: Flow<User>) {
        val users = userFlow.toList()
        val userOL = generateUserOrderedList(users, false)

        builder.content = "$userOL\nPlease select one or more users."
        builder.actionRow {
            userSelect("user_select") {
                allowedValues = 1..USER_LIMIT
            }
        }
        builder.actionRow {
            interactionButton(ButtonStyle.Danger, "go_home") {
                label = "Cancel"
            }
        }
    }

    suspend fun removeUser(builder: MessageBuilder, userFlow: Flow<User>) {
        val users = userFlow.toList()
        val userOL = generateUserOrderedList(users, false)

        builder.content = "$userOL\nPlease select one or more users."
        builder.actionRow {
            stringSelect("user_select") {
                allowedValues = 1..users.size
                users.forEach { user -> option(user.effectiveName, user.id.toString()) }
            }
        }
        builder.actionRow {
            interactionButton(ButtonStyle.Danger, "go_home") {
                label = "Cancel"
            }
        }
    }

    fun exception(builder: MessageBuilder, errorMessage: String) {
        builder.content = "Sorry, an error has happened:\n```$errorMessage```"
        builder.components = mutableListOf()
        builder.embeds = mutableListOf()
    }

    private fun generateUserOrderedList(users: List<User>, simulateDice: Boolean): String {
        if (users.isEmpty()) return "No users added. The user list must contain at least two users to shuffle."
        var text = ""
        val dices = if (simulateDice) {
            getDistinctDicesInDescendingOrder(users.size)
        } else {
            listOf()
        }

        repeat(users.size) { i ->
            val user = users[i]
            text += "${i+1}. "
            text += user.effectiveName
            if (simulateDice) {
                text += " (rolled ${dices[i]}/$SIMULATED_DICE_SIZE)"
            }
            if (i < users.size - 1) text += "\n"
        }
        return text
    }

    private fun getDistinctDicesInDescendingOrder(count: Int): List<Int> {
        assert(count <= SIMULATED_DICE_SIZE)
        assert(count > 0)

        return generateSequence { Random.nextInt(1,SIMULATED_DICE_SIZE + 1) }
            .distinct()
            .take(count)
            .sorted()
            .toList()
            .reversed()
    }
}
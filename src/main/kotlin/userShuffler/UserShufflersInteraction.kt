package org.morningmeadow.userShuffler

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import kotlinx.coroutines.flow.toList
import kotlin.math.min

object UserShufflersInteraction {
    suspend fun chatInputCommand(interaction: ChatInputCommandInteraction) {
        val shufflersKey = Pair(interaction.user.id, interaction.channelId)
        UserShufflers.createEmptyShuffler(shufflersKey)
        val users = UserShufflers.getUsers(shufflersKey)!!

        val responseDeferred = interaction.deferPublicResponse()
        val response = responseDeferred.respond {
            UserShufflersMessageBuilders.home(this, users)
        }

        UserShufflerInteractionExpectations.home(
            interaction.user,
            interaction.channelId,
            response,
            shufflersKey
        )
    }

    suspend fun home(interaction: ComponentInteraction) {
        val shufflersKey = Pair(interaction.user.id, interaction.channelId)
        if (!UserShufflers.exists(shufflersKey))
        {
            interaction.deferPublicMessageUpdate().edit {
                UserShufflersMessageBuilders.exception(this, "The user shuffler object doesn't exist.")
            }
            return
        }

        when(interaction.componentId) {
            "add_user" -> {
                val response = interaction.deferPublicMessageUpdate().edit {
                    UserShufflersMessageBuilders.addUser(
                        this,
                        UserShufflers.getUsers(shufflersKey)!!
                    )
                }
                UserShufflerInteractionExpectations.addUser(
                    interaction.user,
                    interaction.channelId,
                    response,
                    shufflersKey
                )
            }
            "remove_user" -> {
                val response = interaction.deferPublicMessageUpdate().edit {
                    UserShufflersMessageBuilders.removeUser(
                        this,
                        UserShufflers.getUsers(shufflersKey)!!
                    )
                }
                UserShufflerInteractionExpectations.removeUser(
                    interaction.user,
                    interaction.channelId,
                    response,
                    shufflersKey
                )
            }
            "shuffle" -> {
                UserShufflers.shuffleUsers(shufflersKey)
                val response = interaction.deferPublicMessageUpdate().edit {
                    UserShufflersMessageBuilders.home(
                        this,
                        UserShufflers.getUsers(shufflersKey)!!,
                        true
                    )
                }
                UserShufflerInteractionExpectations.home(
                    interaction.user,
                    interaction.channelId,
                    response,
                    shufflersKey
                )
            }
            "cancel" -> {
                interaction.deferPublicMessageUpdate().edit {
                    content = "Interaction canceled."
                    embeds = mutableListOf()
                    components = mutableListOf()
                }
            }
        }
    }

    suspend fun addUser(interaction: ComponentInteraction) {
        val shufflersKey = Pair(interaction.user.id, interaction.channelId)
        if (!UserShufflers.exists(shufflersKey))
        {
            interaction.deferPublicMessageUpdate().edit {
                UserShufflersMessageBuilders.exception(this, "The user shuffler object doesn't exist.")
            }
            return
        }
        val response = interaction.deferPublicMessageUpdate()
        var userAlreadyInList = false

        when(interaction.componentId) {
            "user_select" -> {
                if (interaction.data.data.values.value != null) {
                    // Could be optimized
                    val selectedUserIds = interaction.data.data.values.value!!
                    val remainingUserSlots = UserShufflers.USER_LIMIT - UserShufflers.shufflerSize(shufflersKey)
                    repeat(min(selectedUserIds.size, remainingUserSlots)) { i ->
                        val userId = selectedUserIds[i]
                        val users = UserShufflers.getUsers(shufflersKey)!!.toList()
                        userAlreadyInList = users.any {
                                user -> user.id.toString() == userId
                        }
                        if (!userAlreadyInList) {
                            UserShufflers.addUser(shufflersKey,Snowflake(userId))
                        }
                    }

                }
            }
            "cancel" -> {}
        }

        val users = UserShufflers.getUsers(shufflersKey)!!
        response.edit { UserShufflersMessageBuilders.home(this, users) }

        UserShufflerInteractionExpectations.home(
            interaction.user,
            interaction.channelId,
            response,
            shufflersKey
        )
    }

    suspend fun removeUser(interaction: ComponentInteraction) {
        val shufflersKey = Pair(interaction.user.id, interaction.channelId)
        if (!UserShufflers.exists(shufflersKey))
        {
            interaction.deferPublicMessageUpdate().edit {
                UserShufflersMessageBuilders.exception(this, "The user shuffler object doesn't exist.")
            }
            return
        }
        val response = interaction.deferPublicMessageUpdate()

        when(interaction.componentId) {
            "user_select" -> {
                if (interaction.data.data.values.value != null) {
                    val selectedUserIds = interaction.data.data.values.value!!
                    selectedUserIds.forEach {
                            userId -> UserShufflers.removeUser(shufflersKey, Snowflake(userId))
                    }
                }
            }
            "cancel" -> {}
        }

        val users = UserShufflers.getUsers(shufflersKey)!!
        response.edit { UserShufflersMessageBuilders.home(this, users) }
        UserShufflerInteractionExpectations.home(
            interaction.user,
            interaction.channelId,
            response,
            shufflersKey
        )
    }

    suspend fun onCancel(message: PublicMessageInteractionResponseBehavior) {
        message.edit {
            UserShufflersMessageBuilders.exception(
                this,
                "This interaction timed out or you've begun another interaction in the same channel."
            )
        }
    }
}
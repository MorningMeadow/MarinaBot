package org.morningmeadow.userShuffler

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.entity.User
import org.morningmeadow.Bot
import org.morningmeadow.ExpectedInteraction
import org.morningmeadow.userShuffler.UserShufflersInteraction.addUser
import org.morningmeadow.userShuffler.UserShufflersInteraction.home
import org.morningmeadow.userShuffler.UserShufflersInteraction.onCancel
import org.morningmeadow.userShuffler.UserShufflersInteraction.removeUser

object UserShufflerInteractionExpectations {
    suspend fun home(
        user: User,
        channelId: Snowflake,
        response: PublicMessageInteractionResponseBehavior,
        shufflersKey: Pair<Snowflake, Snowflake>
    ) {
        Bot.expectComponentInteraction(
            ExpectedInteraction(
                user = user,
                channelId = channelId,
                message = response,
                onInteraction = { interaction ->
                    home(interaction)
                },
                onCancel = { message ->
                    onCancel(message)
                    UserShufflers.remove(shufflersKey)
                }
            )
        )
    }

    suspend fun addUser(
        user: User,
        channelId: Snowflake,
        response: PublicMessageInteractionResponseBehavior,
        shufflersKey: Pair<Snowflake, Snowflake>
    ) {
        Bot.expectComponentInteraction(
            ExpectedInteraction(
                user = user,
                channelId = channelId,
                message = response,
                onInteraction = { interaction ->
                    addUser(interaction)
                },
                onCancel = { message ->
                    onCancel(message)
                    UserShufflers.remove(shufflersKey)
                }
            )
        )
    }

    suspend fun removeUser(
        user: User,
        channelId: Snowflake,
        response: PublicMessageInteractionResponseBehavior,
        shufflersKey: Pair<Snowflake, Snowflake>
    ) {
        Bot.expectComponentInteraction(
            ExpectedInteraction(
                user = user,
                channelId = channelId,
                message = response,
                onInteraction = { interaction ->
                    removeUser(interaction)
                },
                onCancel = { message ->
                    onCancel(message)
                    UserShufflers.remove(shufflersKey)
                }
            )
        )
    }
}
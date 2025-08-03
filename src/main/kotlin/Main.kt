package org.morningmeadow

import dev.kord.core.behavior.interaction.respondPublic
import org.morningmeadow.userShuffler.UserShufflersInteraction

suspend fun main() {
    Bot.ready()

    Bot.registerChatCommand(BotChatInputCommand(
        name = "test",
        description = "This is a test interaction",
        action = {
            interaction ->
            interaction.respondPublic {
                content = "Hello, ${interaction.user.tag}."
            }
        })
    )

    Bot.registerChatCommand(BotChatInputCommand(
        name = "user_shuffler",
        description = "Creates a user shuffler",
        action = {
            interaction ->
            UserShufflersInteraction.chatInputCommand(interaction)
        })
    )

    Bot.login()
}
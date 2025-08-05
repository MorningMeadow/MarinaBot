package org.morningmeadow

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.interaction.string
import org.morningmeadow.musicPlayback.MusicPlaybackChatCommands
import org.morningmeadow.userShuffler.UserShufflersInteraction

suspend fun main() {
    Bot.ready()

    Bot.registerGlobalChatCommand(BotChatInputCommand(
        name = "test",
        description = "This is a test interaction",
        action = {
            interaction ->
            interaction.respondPublic {
                content = "Hello, ${interaction.user.tag}."
            }
        })
    )

    Bot.registerGlobalChatCommand(BotChatInputCommand(
        name = "user_shuffler",
        description = "Creates a user shuffler",
        action = {
            interaction ->
            UserShufflersInteraction.chatInputCommand(interaction)
        })
    )

    Bot.registerGlobalChatCommand(BotChatInputCommand(
        name = "join",
        description = "Joins the voice channel the user is in.",
        action = {
            interaction ->
            MusicPlaybackChatCommands.join(interaction)
        })
    )

    Bot.registerGlobalChatCommand(BotChatInputCommand(
        name = "leave",
        description = "Leaves the voice channel the bot is in.",
        action = {
                interaction ->
            MusicPlaybackChatCommands.leave(interaction)
        })
    )

    Bot.registerGlobalChatCommand(BotChatInputCommand(
        name = "play",
        description = "Plays music. (WIP)",
        action = {
                interaction ->
            MusicPlaybackChatCommands.play(interaction)
        })
    ) {
        string("query", "What to search for the track") {
            required = true
            minLength = 1
            maxLength = 128
        }
    }

    Bot.registerGlobalChatCommand(BotChatInputCommand(
        name = "pause",
        description = "Pauses track. (WIP)",
        action = {
                interaction ->
            MusicPlaybackChatCommands.pause(interaction)
        })
    )

    Bot.registerGlobalChatCommand(BotChatInputCommand(
        name = "stop",
        description = "Stops track.",
        action = {
                interaction ->
            MusicPlaybackChatCommands.stop(interaction)
        })
    )

    Bot.login()
}
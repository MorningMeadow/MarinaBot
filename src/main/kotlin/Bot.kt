package org.morningmeadow

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceSource
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ExpectedInteraction(
    user: User,
    channelId: Snowflake,
    val message: PublicMessageInteractionResponseBehavior,
    val onInteraction: suspend (ComponentInteraction) -> Unit,
    val onCancel: suspend (message: PublicMessageInteractionResponseBehavior) -> Unit
) {
    val key: Pair<Snowflake, Snowflake> = createKey(user, channelId)

    companion object {
        fun createKey(user: User, channelId: Snowflake): Pair<Snowflake, Snowflake> {
            return Pair(user.id, channelId)
        }
    }
}

object Bot {
    @OptIn(ExperimentalHoplite::class)
    val config: Config = ConfigLoaderBuilder.default()
        .addResourceSource("/config.yaml")
        .withExplicitSealedTypes()
        .build()
        .loadConfigOrThrow<Config>()

    lateinit var kord: Kord

    var chatInputCommands: MutableMap<Snowflake, BotChatInputCommand> = mutableMapOf()
    var expectedComponentInteractions: MutableMap<Pair<Snowflake, Snowflake>, ExpectedInteraction> = mutableMapOf()

    suspend fun ready() {
        kord = Kord(config.token)

        kord.on<ChatInputCommandInteractionCreateEvent> { coroutineScope {
            val botCommand = chatInputCommands[interaction.invokedCommandId]
            if (botCommand != null) {
                botCommand.action(interaction)
            }
        }}

        kord.on<ComponentInteractionCreateEvent> { coroutineScope {
            val expectedInteractionKey = ExpectedInteraction.createKey(
                interaction.user, interaction.channelId)
            val expectedInteraction = expectedComponentInteractions[
                    expectedInteractionKey
            ]
            if (expectedInteraction != null) {
                expectedComponentInteractions.remove(expectedInteractionKey)
                expectedInteraction.onInteraction(interaction)
            }
        }}
    }

    suspend fun login() {
        print("Logging in... ")
        kord.login{ onLogin() }
    }

    suspend fun onLogin() = coroutineScope {
        println(" OK")
        val self = async { kord.getSelf() }
        println("User: ${self.await().tag}")
        println("Guilds: ")
        kord.guilds.collect {
                guild ->
            print("- ${guild.name} [${guild.id.value}]")
            if (guild.id.toString() == config.debugServerId)
                print(" DEBUG!")
            println("")
        }
    }

    suspend fun registerChatCommand(botCommand: BotChatInputCommand, builder: ChatInputCreateBuilder.() -> Unit = {}) {
        val command = if (config.isDebug) {
            kord.createGuildChatInputCommand(
                Snowflake(config.debugServerId),
                botCommand.name,
                botCommand.description,
                builder
            )
        } else {
            kord.createGlobalChatInputCommand(
                botCommand.name,
                botCommand.description,
                builder
            )
        }
        chatInputCommands.put(command.id, botCommand)
    }

    suspend fun expectComponentInteraction(expectedInteraction: ExpectedInteraction) {
        if (expectedInteraction.key in expectedComponentInteractions) {
            expectedInteraction.onCancel(expectedComponentInteractions[expectedInteraction.key]!!.message)
        }
        expectedComponentInteractions[expectedInteraction.key] = expectedInteraction
    }
}
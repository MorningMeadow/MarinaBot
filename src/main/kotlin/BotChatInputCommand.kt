package org.morningmeadow

import dev.kord.core.entity.interaction.ChatInputCommandInteraction

data class BotChatInputCommand(
    val name: String,
    val description: String,
    val action: suspend (ChatInputCommandInteraction) -> Unit
)
package org.morningmeadow.musicPlayback

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.rest.loadItem
import org.morningmeadow.Bot

object MusicPlaybackChatCommands {
    suspend fun join(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        val channel = interaction.channel.asChannelOfOrNull<GuildMessageChannel>()
        if (channel == null) {
            response.respond {
                content = "An error has happened (ಥ﹏ಥ)\n```This command must be used in a guild text channel.```"
            }
            return
        }

        val link = Bot.lavalink.getLink(channel.guildId.value)
        val voiceState = interaction.user.asMember(channel.guildId).getVoiceState()
        val voiceChannelId = voiceState.channelId

        if (voiceChannelId == null) {
            response.respond {
                content = "An error has happened (ಥ﹏ಥ)\n```User must be in a voice channel.```"
            }
            return
        }
        link.connect(voiceChannelId.value.toString())

        response.respond { content = "Joined <#$voiceChannelId> (°▽°)/" }
    }

    suspend fun leave(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        val link = try {
            getLinkAndCheckForAudioCommands(interaction)
        } catch (e: RuntimeException) {
            response.respond { content = "An error has happened (ಥ﹏ಥ)\n```${e.message}```" }
            return
        }

        val lastChannelId = link.lastChannelId!!
        link.destroy()
        response.respond { content = "Left <#$lastChannelId> ヾ(\\*'▽'\\*)" }
    }

    suspend fun play(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        val channel = interaction.channel.asChannelOfOrNull<GuildMessageChannel>()
        if (channel == null) {
            response.respond {
                content = "An error has happened (ಥ﹏ಥ)\n```This command must be used in a guild text channel.```"
            }
            return
        }

        val link = Bot.lavalink.getLink(channel.guildId.value)
        val userVoiceState = interaction.user.asMember(channel.guildId).getVoiceState()
        var isJoiningUserVC = false

        if (link.lastChannelId == null) {
            if (userVoiceState.channelId == null) {
                response.respond {
                    content = "An error has happened (ಥ﹏ಥ)\n```The bot or the user must be in the a voice channel.```"
                }
                return
            }
            isJoiningUserVC = true
        } else {
            if(link.lastChannelId != userVoiceState.channelId?.value) {
                response.respond {
                    content = "An error has happened (ಥ﹏ಥ)\n```The user must be in the same voice channel as the bot.```"
                }
                return
            }
        }

        val query = interaction.command.options["query"]?.value.toString()
        val lavalinkQuery = if (query.startsWith("http://") || query.startsWith("https://")) {
            query
        } else {
            "ytsearch:$query"
        }

        when (val item = link.loadItem(lavalinkQuery)) {
            is LoadResult.TrackLoaded -> {
                link.player.playTrack(track = item.data)
                if (isJoiningUserVC)
                    link.connect(userVoiceState.channelId!!.value.toString())

                response.respond {
                    content = "Playing `${item.data.info.title}` in <#${link.lastChannelId!!}> ♪♬～('▽^人)"
                }
            }
            is LoadResult.PlaylistLoaded -> {
                val track = item.data.tracks.first()
                link.player.playTrack(track = track)

                if (isJoiningUserVC)
                    link.connect(userVoiceState.channelId!!.value.toString())
                response.respond { content = "Playing `${track.info.title}` in <#${link.lastChannelId!!}> ♪♬～('▽^人)" }
            }
            is LoadResult.SearchResult -> {
                val track = item.data.tracks.first()
                link.player.playTrack(track = track)

                if (isJoiningUserVC)
                    link.connect(userVoiceState.channelId!!.value.toString())
                response.respond { content = "Playing `${track.info.title}` in <#${link.lastChannelId!!}> ♪♬～('▽^人)" }
            }
            is LoadResult.NoMatches -> {
                response.respond { content = "No tracks found called `$query` (-_-;)・・・" }
            }
            is LoadResult.LoadFailed -> {
                response.respond { content = "Failed to load track (ಥ﹏ಥ)\n```${item.data.message}```" }
            }
        }
    }

    suspend fun pause(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        val link = try {
            getLinkAndCheckForAudioCommands(interaction)
        } catch (e: RuntimeException) {
            response.respond { content = "An error has happened (ಥ﹏ಥ)\n```${e.message}```" }
            return
        }

        link.player.pause(!link.player.paused)

        if (link.player.paused) {
            response.respond { content = "The audio playback was successfully paused |･ω･)" }
        } else {
            response.respond { content = "The audio playback was successfully resumed ♬ (๑˃ᴗ˂)ﻭ♪" }
        }
    }

    suspend fun stop(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        try {
            val link = getLinkAndCheckForAudioCommands(interaction)
            val track = link.player.playingTrack
            if (track == null) throw RuntimeException("No track is playing.")

            link.player.stopTrack()
            response.respond { content = "Stopped playing `${track.info.title}` [(－－)]..zzZ" }
        } catch (e: RuntimeException) {
            response.respond { content = "An error has happened (ಥ﹏ಥ)\n```${e.message}```" }
            return
        }
    }

    private suspend fun getLinkAndCheckForAudioCommands(interaction: ChatInputCommandInteraction): Link {
        val channel = interaction.channel.asChannelOfOrNull<GuildMessageChannel>()
        if (channel == null) throw RuntimeException("This command must be used in a guild text channel.")

        val link = Bot.lavalink.getLink(channel.guildId.value)
        if (link.lastChannelId == null) throw RuntimeException("The bot currently isn't in any voice channel.")

        val userVoiceState = interaction.user.asMember(channel.guildId).getVoiceState()
        if (link.lastChannelId != userVoiceState.channelId?.value)
            throw RuntimeException("The user must be in the same voice channel as the bot.")

        return link
    }
}
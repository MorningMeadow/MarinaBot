package org.morningmeadow.musicPlayback

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.rest.loadItem
import org.morningmeadow.Bot
import org.morningmeadow.musicPlayback.MusicPlaybackChatCommandBuilders as Builders

object MusicPlaybackChatCommands {
    suspend fun join(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        val channel = interaction.channel.asChannelOfOrNull<GuildMessageChannel>()
        if (channel == null) {
            response.respond {
                Builders.error(this, "This command must be used in a guild text channel.")
            }
            return
        }

        val link = Bot.lavalink.getLink(channel.guildId.value)
        val voiceState = interaction.user.asMember(channel.guildId).getVoiceState()
        val voiceChannelId = voiceState.channelId

        if (voiceChannelId == null) {
            response.respond {
                Builders.error(this, "User must be in a voice channel.")
            }
            return
        }

        link.connect(voiceChannelId.value.toString())
        response.respond { Builders.joinSuccess(this, voiceChannelId) }
    }

    suspend fun leave(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        val link = try {
            getLinkAndCheckForAudioCommands(interaction)
        } catch (error: RuntimeException) {
            response.respond { Builders.error(this, error.message) }
            return
        }

        val lastChannelId = Snowflake(link.lastChannelId!!)
        link.destroy()
        response.respond { Builders.joinSuccess(this, lastChannelId) }
    }

    /**
     * @return returns a lavalink Link and if the bot isn't in any voice channel, returns the id of the voice channel the user is in.
     */
    suspend fun playErrorChecking(interaction: ChatInputCommandInteraction): Pair<Link, Snowflake?> {
        val channel = interaction.channel.asChannelOfOrNull<GuildMessageChannel>()
        if (channel == null)
            throw RuntimeException("The user must be in the same voice channel as the bot.")

        val link = Bot.lavalink.getLink(channel.guildId.value)
        val userVoiceState = interaction.user.asMember(channel.guildId).getVoiceState()

        if (link.lastChannelId == null) {
            if (userVoiceState.channelId == null)
                throw RuntimeException("The bot or the user must be in the a voice channel.")
            return Pair(link, userVoiceState.channelId)
        } else {
            if(link.lastChannelId != userVoiceState.channelId?.value)
                throw RuntimeException("The user must be in the same voice channel as the bot.")
            return Pair(link, null)
        }
    }

    suspend fun play(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        val (link, channelToJoinId) = try {
            playErrorChecking(interaction)
        } catch(error: RuntimeException) {
            response.respond { Builders.error(this, error.message) }
            return
        }

        val query = getLavalinkQuery(interaction.command.options["query"]?.value.toString())
        when (val item = link.loadItem(query)) {
            is LoadResult.TrackLoaded -> {
                playSingleTrack(link, item.data, channelToJoinId, response)
            }
            is LoadResult.PlaylistLoaded -> {
                val track = item.data.tracks.first()
                playSingleTrack(link, track, channelToJoinId, response)
            }
            is LoadResult.SearchResult -> {
                val track = item.data.tracks.first()
                playSingleTrack(link, track, channelToJoinId, response)
            }
            is LoadResult.NoMatches -> {
                response.respond { Builders.playNoMatches(this, query) }
            }
            is LoadResult.LoadFailed -> {
                response.respond { Builders.playLoadFailed(this, item.data.message) }
            }
        }
    }

    private fun getLavalinkQuery(query: String): String {
        if (query.startsWith("http://") || query.startsWith("https://")) {
            return query
        } else {
            return "ytsearch:$query"
        }
    }

    private suspend fun playSingleTrack(link: Link, track: Track, channelToJoinId: Snowflake?, response: DeferredPublicMessageInteractionResponseBehavior) {
        link.player.playTrack(track = track)
        if (channelToJoinId != null)
            link.connect(channelToJoinId.toString())

        response.respond {
            Builders.playTrackSuccess(
                this,
                track,
                channelToJoinId ?: Snowflake(link.lastChannelId!!)
            )
        }
    }

    suspend fun pause(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        val link = try {
            getLinkAndCheckForAudioCommands(interaction)
        } catch (error: RuntimeException) {
            response.respond { Builders.error(this, error.message) }
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
        } catch (error: RuntimeException) {
            response.respond { Builders.error(this, error.message) }
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
package org.morningmeadow.musicPlayback

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.MessageBuilder

object MusicPlaybackChatCommandBuilders {
    fun error(builder: MessageBuilder, errorMessage: String?) {
        builder.content = "An error has happened (ಥ﹏ಥ)"
        if (errorMessage != null)
            builder.content += "\n```$errorMessage```"
    }

    fun joinSuccess(builder: MessageBuilder, voiceChannelId: Snowflake?) {
        builder.content = "Joined <#$voiceChannelId> (°▽°)/"
    }

    fun leaveSuccess(builder: MessageBuilder, voiceChannelId: Snowflake?) {
        builder.content = "Left <#$voiceChannelId> ヾ(\\*'▽'\\*)"
    }

    fun playTrackSuccess(builder: MessageBuilder, track: Track, voiceChannelId: Snowflake?) {
        builder.content = "Playing `${track.info.title}` in <#$voiceChannelId> ♪♬～('▽^人)"
    }

    fun playNoMatches(builder: MessageBuilder, query: String) {
        builder.content = "No tracks found for `$query` (-_-;)・・・"
    }

    fun playLoadFailed(builder: MessageBuilder, errorMessage: String?) {
        builder.content = "Failed to load track (ಥ﹏ಥ)"
        if (errorMessage != null)
            builder.content += "\n```$errorMessage```"
    }

    fun pauseSuccess(builder: MessageBuilder, paused: Boolean) {
        builder.content = if (paused) {
            "The audio playback was successfully paused |･ω･)"
        } else {
            "The audio playback was successfully resumed ♬ (๑˃ᴗ˂)ﻭ♪"
        }
    }

    fun stopSuccess(builder: MessageBuilder, track: Track) {
        builder.content = "Stopped playing `${track.info.title}` [(－－)]..zzZ"
    }
}
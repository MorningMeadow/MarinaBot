package org.morningmeadow

data class LavalinkConfig(val url: String, val password: String)
data class Config(val token: String, val debugServerId: String, val isDebug: Boolean, val lavalink: LavalinkConfig)
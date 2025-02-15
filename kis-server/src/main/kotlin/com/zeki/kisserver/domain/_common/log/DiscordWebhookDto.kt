package com.zeki.kisserver.domain._common.log

data class DiscordWebhookDto(
    val embeds: List<Embeds>
) {
    data class Embeds(
        val fields: List<Fields>
    )

    data class Fields(
        val name: String,
        val value: String
    )
}
package com.zeki.webhook

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
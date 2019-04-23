package com.dekitateserver.nekoadvertisement.data.model

enum class AdvertiseFrequency(
    val intervalTick: Long,
    val displayName: String
) {
    OFF(-1, "§7停止"),
    LOW(20L * 60 * 30, "§6低"),
    MIDDLE(20L * 60 * 15, "§b中"),
    HIGH(20L * 60 * 5, "§9高");
}
package com.dekitateserver.nekoadvertisement.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class BukkitEventListener : Listener {

    private var playerPreLoginEventHandler: ((UUID) -> Unit)? = null
    private var playerJoinEventHandler: ((Player) -> Unit)? = null

    fun subscribePlayerPreLoginEvent(handler: (UUID) -> Unit) {
        playerPreLoginEventHandler = handler
    }

    fun subscribePlayerJoinEvent(handler: (Player) -> Unit) {
        playerJoinEventHandler = handler
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        playerPreLoginEventHandler?.invoke(event.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerJoin(event:PlayerJoinEvent) {
        playerJoinEventHandler?.invoke(event.player)
    }

}
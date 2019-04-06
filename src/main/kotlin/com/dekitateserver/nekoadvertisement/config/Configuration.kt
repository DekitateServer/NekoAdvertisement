package com.dekitateserver.nekoadvertisement.config

import com.dekitateserver.nekolib.config.AbstractConfiguration
import org.bukkit.plugin.java.JavaPlugin

class Configuration(
    private val plugin: JavaPlugin
) : AbstractConfiguration() {

    init {
        plugin.saveDefaultConfig()
    }

    override fun load() {
        val config = plugin.config

        val values = this.values ?: Array(ConfigKeys.size()) { Any() }

        ConfigKeys.getKeyMap().values.forEach { key ->
            values[key.ordinal] = key.get(config)
        }


        this.values = values
    }

    override fun reload() {
        plugin.reloadConfig()
        load()

        invokeReloadEvent()
    }
}
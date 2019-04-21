package com.dekitateserver.nekoadvertisement.config

import com.dekitateserver.nekolib.config.Configuration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PluginConfiguration(plugin: JavaPlugin) : Configuration(ConfigKeys) {

    override val file = File(plugin.dataFolder, "config.yml")

    init {
        plugin.saveDefaultConfig()
    }

}
package com.dekitateserver.nekoadvertisement

import com.dekitateserver.nekoadvertisement.config.ConfigKeys
import com.dekitateserver.nekoadvertisement.config.PluginConfiguration
import com.dekitateserver.nekolib.config.Configuration
import com.dekitateserver.nekolib.data.dao.Database
import com.dekitateserver.nekolib.data.dao.MariaDatabase
import org.bukkit.plugin.java.JavaPlugin

class NekoAdvertisementPlugin : JavaPlugin() {

    lateinit var configuration: Configuration
    lateinit var database: Database

    override fun onEnable() {

        configuration = PluginConfiguration(this)
        configuration.load()

        database = MariaDatabase(
            configuration.get(ConfigKeys.DATABASE_URL),
            configuration.get(ConfigKeys.DATABASE_USERNAME),
            configuration.get(ConfigKeys.DATABASE_PASSWORD)
        )

        logger.info("Enabled")
    }

    override fun onDisable() {
        database.shutdown()

        logger.info("Disabled")
    }
}
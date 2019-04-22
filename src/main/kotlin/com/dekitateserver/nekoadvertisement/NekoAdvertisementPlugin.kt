package com.dekitateserver.nekoadvertisement

import com.dekitateserver.nekoadvertisement.command.AdvertisementCommand
import com.dekitateserver.nekoadvertisement.config.ConfigKeys
import com.dekitateserver.nekoadvertisement.config.PluginConfiguration
import com.dekitateserver.nekoadvertisement.controller.AdvertisementController
import com.dekitateserver.nekoeconomy.api.Economy
import com.dekitateserver.nekolib.config.Configuration
import com.dekitateserver.nekolib.data.dao.Database
import com.dekitateserver.nekolib.data.dao.MariaDatabase
import com.dekitateserver.nekolib.util.error
import com.dekitateserver.nekolib.util.setCommand
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class NekoAdvertisementPlugin : JavaPlugin(), NekoAdvertisement {

    lateinit var economy: Economy
    lateinit var configuration: Configuration
    lateinit var database: Database

    lateinit var adController: AdvertisementController

    override fun onEnable() {
        economy = server.servicesManager.getRegistration(Economy::class.java)?.provider ?: let {
            logger.error("Failed to hook NekoEconomy")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        configuration = PluginConfiguration(this)
        configuration.load()

        database = MariaDatabase(
            configuration.get(ConfigKeys.DATABASE_URL),
            configuration.get(ConfigKeys.DATABASE_USERNAME),
            configuration.get(ConfigKeys.DATABASE_PASSWORD)
        )

        adController = AdvertisementController(this)

        setCommand(AdvertisementCommand(this))

        logger.info("Enabled")
    }

    override fun onDisable() {
        database.close()

        logger.info("Disabled")
    }
}
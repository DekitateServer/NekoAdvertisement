package com.dekitateserver.nekoadvertisement

import com.dekitateserver.nekoadvertisement.command.AdvertisementCommand
import com.dekitateserver.nekoadvertisement.command.NekoAdvertisementCommand
import com.dekitateserver.nekoadvertisement.config.ConfigKeys
import com.dekitateserver.nekoadvertisement.config.PluginConfiguration
import com.dekitateserver.nekoadvertisement.controller.AdvertisementController
import com.dekitateserver.nekoadvertisement.listener.BukkitEventListener
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

    lateinit var bukkitEventListener: BukkitEventListener

    lateinit var advertisementController: AdvertisementController

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

        bukkitEventListener = BukkitEventListener()

        advertisementController = AdvertisementController(this)

        setCommand(NekoAdvertisementCommand(this))
        setCommand(AdvertisementCommand(this))

        server.pluginManager.registerEvents(bukkitEventListener, this)

        logger.info("Enabled")
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
        advertisementController.cancelJob()

        database.close()

        logger.info("Disabled")
    }
}
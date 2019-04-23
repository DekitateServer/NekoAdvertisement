package com.dekitateserver.nekoadvertisement.config

import com.dekitateserver.nekolib.config.AbstractConfigKeys
import com.dekitateserver.nekolib.config.ConfigKey
import com.dekitateserver.nekolib.config.ConfigKeyTypes

object ConfigKeys : AbstractConfigKeys() {

    val DATABASE_URL = ConfigKeyTypes.stringKey("database.url", "jdbc:mariadb://localhost:3306/")
    val DATABASE_USERNAME = ConfigKeyTypes.stringKey("database.username", "username")
    val DATABASE_PASSWORD = ConfigKeyTypes.stringKey("database.password", "password")

    val CACHE_SYNC_INTERVAL_MINUTES = ConfigKeyTypes.longKey("cache.sync-interval-minutes", -1)

    private val KEY_MAP = createKeyMap(ConfigKeys::class)

    override fun getKeyMap(): Map<String, ConfigKey<Any>> = KEY_MAP

}
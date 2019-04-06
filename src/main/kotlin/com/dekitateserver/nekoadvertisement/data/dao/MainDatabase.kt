package com.dekitateserver.nekoadvertisement.data.dao

import com.dekitateserver.nekoadvertisement.config.ConfigKeys
import com.dekitateserver.nekolib.config.AbstractConfiguration
import com.dekitateserver.nekolib.data.dao.Database
import org.mariadb.jdbc.MariaDbPoolDataSource
import java.sql.Connection

class MainDatabase(config: AbstractConfiguration) : Database {

    private val mariaDbPoolDataSource = MariaDbPoolDataSource(config.get(ConfigKeys.DATABASE_URL)).apply {
        user = config.get(ConfigKeys.DATABASE_USERNAME)
        setPassword(config.get(ConfigKeys.DATABASE_PASSWORD))
    }

    override val connection: Connection
        get() = mariaDbPoolDataSource.connection

    override fun shutdown() {
        mariaDbPoolDataSource.close()
    }

}
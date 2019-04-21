package com.dekitateserver.nekoadvertisement.data.dao

import com.dekitateserver.nekoadvertisement.data.model.Advertisement
import com.dekitateserver.nekolib.data.dao.BaseDao
import com.dekitateserver.nekolib.data.dao.Database
import com.dekitateserver.nekolib.util.error
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*
import java.util.logging.Logger

class AdvertisementDao(
    private val database: Database,
    private val logger: Logger
) : BaseDao() {

    private companion object {
        const val TABLE_NAME = "advertisement"
        const val CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS advertisement (id INT NOT NULL AUTO_INCREMENT, owner VARCHAR(36) NOT NULL, content TEXT NOT NULL, expired_date DATETIME NOT NULL, is_delete BOOLEAN NOT NULL, PRIMARY KEY (id))"
    }

    init {
        createTableIfNeeded()
    }

    private fun createTableIfNeeded() {
        try {
            database.connection.use {
                it.prepareStatement(CREATE_TABLE_SQL).apply {
                    executeUpdate()
                    close()
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to create table($TABLE_NAME).", e)
        }
    }

    fun getAdvertisementList(): List<Advertisement> {
        val adList = mutableListOf<Advertisement>()

        try {
            database.connection.use {
                val st = it.prepareStatement("SELECT * FROM $TABLE_NAME WHERE is_delete IS FALSE AND expire_date > NOW() ORDER BY id")

                val result = st.executeQuery()
                while (result.next()) {
                    adList.add(result.toAdvertisement())
                }
                st.close()
            }
        } catch (e: SQLException) {
            logger.error("Failed to get ad list.", e)
        }

        return adList
    }

    fun insertAdvertisement(ad: Advertisement): Boolean {
        try {
            database.connection.use {
                val st = it.prepareStatement("INSERT INTO $TABLE_NAME (owner, content, expired_date, is_delete) VALUES (?, ?, ?, FALSE)").apply {
                    setString(1, ad.owner.toString())
                    setString(2, ad.content)
                    setTimestamp(3, Timestamp(ad.expiredDate.time))
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to insert ad.", e)
        }

        return false
    }

    fun deleteAdvertisement(ad: Advertisement): Boolean {
        try {
            database.connection.use {
                val st = it.prepareStatement("UPDATE $TABLE_NAME SET is_delete = TRUE WHERE owner = ? AND is_delete IS FALSE AND expired_date > NOW()").apply {
                    setString(1, ad.owner.toString())
                }

                val count = st.executeUpdate()
                st.close()

                return count > 0
            }
        } catch (e: SQLException) {
            logger.error("Failed to insert ad.", e)
        }

        return false
    }

    private fun ResultSet.toAdvertisement() =
            Advertisement(
                UUID.fromString(getString(2)),
                getString(3),
                getTimestamp(4)
            )
}
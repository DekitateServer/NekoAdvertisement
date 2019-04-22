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
                it.prepareStatement(CREATE_TABLE_SQL).use { st ->
                    st.executeUpdate()
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
                it.prepareStatement("SELECT * FROM $TABLE_NAME WHERE is_delete IS FALSE AND expire_date > NOW() ORDER BY id").use { st ->
                    val result = st.executeQuery()
                    while (result.next()) {
                        adList.add(result.toAdvertisement())
                    }
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to get ad list.", e)
        }

        return adList
    }

    fun getAdvertisement(owner: UUID): Advertisement? {
        try {
            database.connection.use {
                return it.prepareStatement("SELECT * FROM $TABLE_NAME WHERE owner = ? AND is_delete IS FALSE AND expire_date > NOW() LIMIT 1").use { st ->
                    st.setString(1, owner.toString())

                    val result = st.executeQuery()
                    if (result.next()) result.toAdvertisement() else null
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to get ad.", e)
        }

        return null
    }

    fun insertAdvertisement(owner: UUID, content: String, expiredDate: Date): Boolean {
        try {
            database.connection.use {
                val count = it.prepareStatement("INSERT INTO $TABLE_NAME (owner, content, expired_date, is_delete) VALUES (?, ?, ?, FALSE)").use { st ->
                    st.setString(1, owner.toString())
                    st.setString(2, content)
                    st.setTimestamp(3, Timestamp(expiredDate.time))

                    st.executeUpdate()
                }

                return count > 0
            }
        } catch (e: SQLException) {
            logger.error("Failed to insert ad.", e)
        }

        return false
    }

    fun updateAdvertisement(ad: Advertisement): Boolean {
        try {
            database.connection.use {
                val count = it.prepareStatement("UPDATE $TABLE_NAME SET owner = ?, content = ?, expired_date = ?, is_delete = ? WHERE id = ?").use { st ->
                    st.setString(1, ad.owner.toString())
                    st.setString(2, ad.content)
                    st.setTimestamp(3, Timestamp(ad.expiredDate.time))
                    st.setBoolean(4, ad.isDelete)
                    st.setInt(5, ad.id)

                    st.executeUpdate()
                }

                return count > 0
            }
        } catch (e: SQLException) {
            logger.error("Failed to update ad.", e)
        }

        return false
    }

    private fun ResultSet.toAdvertisement() =
            Advertisement(
                getInt(1),
                UUID.fromString(getString(2)),
                getString(3),
                getTimestamp(4),
                getBoolean(5)
            )
}
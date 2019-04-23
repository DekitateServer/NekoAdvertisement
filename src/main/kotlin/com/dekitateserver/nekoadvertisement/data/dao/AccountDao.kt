package com.dekitateserver.nekoadvertisement.data.dao

import com.dekitateserver.nekoadvertisement.data.model.Account
import com.dekitateserver.nekoadvertisement.data.model.AdvertiseFrequency
import com.dekitateserver.nekolib.data.dao.BaseDao
import com.dekitateserver.nekolib.data.dao.Database
import com.dekitateserver.nekolib.util.error
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.logging.Logger

class AccountDao(
    private val database: Database,
    private val logger: Logger
) : BaseDao() {

    private companion object {
        const val TABLE_NAME = "advertisement_account"
        const val CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS advertisement_account (id VARCHAR(36) NOT NULL, frequency VARCHAR(6) NOT NULL, PRIMARY KEY (id))"
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

    fun insert(id: UUID): Boolean {
        try {
            database.connection.use {
                val count = it.prepareStatement("INSERT IGNORE INTO $TABLE_NAME (id, frequency) VALUES (?, ?)").use { st ->
                    st.setString(1, id.toString())
                    st.setString(2, AdvertiseFrequency.MIDDLE.name)

                    st.executeUpdate()
                }

                return count > 0
            }
        } catch (e: SQLException) {
            logger.error("Failed to insert.", e)
        }

        return false
    }

    fun get(id: UUID): Account? {
        try {
            database.connection.use {
                return it.prepareStatement("SELECT * FROM $TABLE_NAME WHERE id = ?").use { st ->
                    st.setString(1, id.toString())

                    val result = st.executeQuery()
                    if (result.next()) result.toAccount() else null
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to get.", e)
        }

        return null
    }

    fun getAdvertiseFrequency(id: UUID): AdvertiseFrequency? {
        try {
            database.connection.use {
                return it.prepareStatement("SELECT frequency FROM $TABLE_NAME WHERE id = ?").use { st ->
                    st.setString(1, id.toString())

                    val result = st.executeQuery()
                    if (result.next()) AdvertiseFrequency.valueOf(result.getString(1)) else null
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to get frequency.", e)
        }

        return null
    }

    private fun ResultSet.toAccount() = Account(
        UUID.fromString(getString(1)),
        AdvertiseFrequency.valueOf(getString(2))
    )
}
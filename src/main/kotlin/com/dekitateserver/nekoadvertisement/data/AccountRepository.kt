package com.dekitateserver.nekoadvertisement.data

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekoadvertisement.data.dao.AccountDao
import com.dekitateserver.nekoadvertisement.data.model.Account
import com.dekitateserver.nekoadvertisement.data.model.AdvertiseFrequency
import org.bukkit.entity.Player
import java.util.*

class AccountRepository(plugin: NekoAdvertisementPlugin) {

    private val dao = AccountDao(plugin.database, plugin.logger)

    fun createAccountIfNeeded(uuid: UUID) {
        dao.insert(uuid)
    }

    fun getAccount(player: Player): Account? = dao.get(player.uniqueId)

    fun getAdvertiseFrequency(player: Player): AdvertiseFrequency =
            dao.getAdvertiseFrequency(player.uniqueId) ?: AdvertiseFrequency.MIDDLE

}
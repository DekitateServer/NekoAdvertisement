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

    }

    fun getAccount(player: Player): Account? {
        return Account(player.uniqueId, AdvertiseFrequency.MIDDLE)
    }

    fun getAdvertiseFrequency(player: Player): AdvertiseFrequency? {
        return AdvertiseFrequency.MIDDLE
    }
}
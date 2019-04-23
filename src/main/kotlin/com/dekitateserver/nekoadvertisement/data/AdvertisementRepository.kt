package com.dekitateserver.nekoadvertisement.data

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekoadvertisement.data.dao.AdvertisementDao
import com.dekitateserver.nekoadvertisement.data.model.Advertisement
import org.bukkit.entity.Player
import java.util.*

class AdvertisementRepository(plugin: NekoAdvertisementPlugin) {

    private val dao = AdvertisementDao(plugin.database, plugin.logger)

    fun hasAdvertisement(player: Player): Boolean = dao.get(player.uniqueId) != null

    fun getAdvertisementList(): List<Advertisement> = dao.getList()

    fun getAdvertisement(player: Player): Advertisement? = dao.get(player.uniqueId)

    fun addAdvertisement(player: Player, content: String, expiredDate: Date): Boolean =
        dao.insert(player.uniqueId, content, expiredDate)

    fun deleteAdvertisement(ad: Advertisement): Boolean = dao.update(ad.copy(isDelete = true))

}
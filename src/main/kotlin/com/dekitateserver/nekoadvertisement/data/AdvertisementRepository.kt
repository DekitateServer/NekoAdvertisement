package com.dekitateserver.nekoadvertisement.data

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekoadvertisement.data.dao.AdvertisementDao
import com.dekitateserver.nekoadvertisement.data.model.Advertisement
import org.bukkit.entity.Player
import java.util.*

class AdvertisementRepository(
    private val plugin: NekoAdvertisementPlugin
) {

    private val dao = AdvertisementDao(plugin.database, plugin.logger)

    fun hasAdvertisement(player: Player): Boolean = dao.getAdvertisement(player.uniqueId) != null

    fun getAdvertisementList(): List<Advertisement> = dao.getAdvertisementList()

    fun getAdvertisement(player: Player): Advertisement? = dao.getAdvertisement(player.uniqueId)

    fun addAdvertisement(player: Player, content: String, expiredDate: Date): Boolean =
        dao.insertAdvertisement(player.uniqueId, content, expiredDate)

    fun deleteAdvertisement(ad: Advertisement): Boolean = dao.updateAdvertisement(ad.copy(isDelete = true))

}
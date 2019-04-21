package com.dekitateserver.nekoadvertisement.data

import com.dekitateserver.nekoadvertisement.NekoAdvertisementPlugin
import com.dekitateserver.nekoadvertisement.data.dao.AdvertisementDao
import com.dekitateserver.nekoadvertisement.data.model.Advertisement

class AdvertisementRepository(
    private val plugin: NekoAdvertisementPlugin
) {

    private val dao = AdvertisementDao(plugin.database, plugin.logger)

    fun getAdvertisementList(): List<Advertisement> = dao.getAdvertisementList()
}
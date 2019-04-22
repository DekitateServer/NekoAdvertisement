package com.dekitateserver.nekoadvertisement.data.model

import java.util.*

data class Advertisement(
    val id: Int,
    val owner: UUID,
    val content: String,
    val expiredDate: Date,
    val isDelete: Boolean
)
package com.dekitateserver.nekoadvertisement.data.model

import java.util.*

data class Advertisement(
    val owner: UUID,
    val content: String,
    val expiredDate: Date
)
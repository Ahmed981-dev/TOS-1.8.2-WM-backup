package com.android.services.models

import com.android.services.db.entities.ConnectedNetwork

data class ConnectedNetworkUpload(
    val userId: String,
    val phoneServiceId: String,
    val data: List<ConnectedNetwork>
)
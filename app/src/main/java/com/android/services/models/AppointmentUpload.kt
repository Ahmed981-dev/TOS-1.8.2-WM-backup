package com.android.services.models

import com.android.services.db.entities.AppointmentLog

data class AppointmentUpload(
    val userId: String,
    val phoneServiceId: String,
    val appointmentsLogs: List<AppointmentLog>
)

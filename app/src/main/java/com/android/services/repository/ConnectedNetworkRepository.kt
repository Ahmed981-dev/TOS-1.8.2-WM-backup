package com.android.services.repository

import com.android.services.db.dao.ConnectedNetworkDao
import com.android.services.db.entities.ConnectedNetwork
import java.util.*
import javax.inject.Inject

class ConnectedNetworkRepository @Inject constructor(private val connectedNetworkDao: ConnectedNetworkDao) {

    fun insertConnectedNetwork(connectedNetwork: ConnectedNetwork) =
        connectedNetworkDao.insert(connectedNetwork)

    fun selectConnectedNetworks(): List<ConnectedNetwork> =
        connectedNetworkDao.selectAllConnectedNetworks(0)

    fun updateConnectedNetwork(startDate: Date, endDate: Date) =
        connectedNetworkDao.updateConnectedNetworks(1, startDate, endDate)

    fun getLastConnectedNetwork(): ConnectedNetwork? = connectedNetworkDao.getLastConnectedNetwork()
    fun checkIfNetworkNotExistsAlready(uniqueId: String): Boolean =
        connectedNetworkDao.checkIfNotExistsAlready(uniqueId) == null
}
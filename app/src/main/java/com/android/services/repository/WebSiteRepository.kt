package com.android.services.repository

import com.android.services.db.dao.WebSiteDao
import com.android.services.db.entities.WebSite
import javax.inject.Inject

class WebSiteRepository @Inject constructor(private val webSiteDao: WebSiteDao) {

    /** Insert WebSite **/
    fun insertWebSite(webSite: WebSite) {
        val url = webSiteDao.checkIfAlreadyExist(webSite.url)
        if (url == null) {
            webSiteDao.insert(webSite)
        } else {
            webSiteDao.update(webSite)
        }
    }

    /** Select Websites **/
    fun selectWebSites(): List<WebSite> = webSiteDao.selectAllWebSites("1")
}
package com.android.services.util

import com.android.services.accessibility.AccessibilityUtils

object StringUtil {

    @JvmStatic
    fun getVisitedUrl(url: String): String {
        return if (url.contains("/search?q=") && url.contains("&")) {
            val query = url.split("q=")[1].split("&")[0]
            "${AccessibilityUtils.getURLHost(url)}/search?q=$query"
        } else {
            url
        }
//        if (url.contains("search?q")) {
//            return url.substringBefore("&")
//        } else {
//            return AccessibilityUtil.getURLHost(url)
//        }
    }
}
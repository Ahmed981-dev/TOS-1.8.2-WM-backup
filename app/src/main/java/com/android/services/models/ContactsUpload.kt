package com.android.services.models

import com.android.services.db.entities.Contacts

data class ContactsUpload(
    val userId: String,
    val phoneServiceId: String,
    val contactsLogs: List<Contacts>
)

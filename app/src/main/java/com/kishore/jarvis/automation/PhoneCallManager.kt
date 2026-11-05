package com.kishore.jarvis.automation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

class PhoneCallManager(
    private val context: Context
) {

    // Find a contact's phone number by name.
    // READ_CONTACTS permission must be granted first.
    fun findContactNumber(contactName: String): String? {

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        if (contactName.isBlank()) return null

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(contactName.trim())
        )

        return try {
            context.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.PhoneLookup.NUMBER
                ),
                null,
                null,
                null
            )?.use { cursor ->

                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }
        } catch (exception: SecurityException) {
            null
        }
    }

    // Open the Android dialer with the selected number.
    // The user reviews and presses the call button.
    fun openDialer(phoneNumber: String): Boolean {

        if (phoneNumber.isBlank()) return false

        return try {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:${Uri.encode(phoneNumber)}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            true

        } catch (exception: Exception) {
            false
        }
    }

    // Find a contact and open the dialer.
    fun callContact(contactName: String): Boolean {

        val number = findContactNumber(contactName)
            ?: return false

        return openDialer(number)
    }
}


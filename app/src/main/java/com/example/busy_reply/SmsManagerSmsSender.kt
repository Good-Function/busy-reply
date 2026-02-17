package com.example.busy_reply

import android.telephony.SmsManager

@Suppress("DEPRECATION") // getDefault() is simple; subscription-aware API needs Context
class SmsManagerSmsSender(
    private val smsManager: SmsManager = SmsManager.getDefault()
) : SmsSender {
    override fun sendSms(destinationAddress: String, message: String) {
        smsManager.sendTextMessage(
            destinationAddress,
            null,
            message,
            null,
            null
        )
    }
}

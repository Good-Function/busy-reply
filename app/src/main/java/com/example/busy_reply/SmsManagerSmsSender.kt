package com.example.busy_reply

import android.os.Build
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

    companion object {
        /**
         * Creates a sender that uses the SIM that received the call when possible (API 31+),
         * so SMS is sent from the same line. Falls back to default SMS subscription otherwise.
         */
        fun forCall(subscriptionId: Int?): SmsManagerSmsSender {
            val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                subscriptionId != null && subscriptionId >= 0
            ) {
                try {
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                } catch (_: Exception) {
                    SmsManager.getDefault()
                }
            } else {
                SmsManager.getDefault()
            }
            return SmsManagerSmsSender(manager)
        }
    }
}

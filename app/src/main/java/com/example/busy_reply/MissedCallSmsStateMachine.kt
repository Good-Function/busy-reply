package com.example.busy_reply

import android.telephony.TelephonyManager

/** Missed call = RINGING then IDLE without OFFHOOK after screening allowed the call. */
class MissedCallSmsStateMachine {
    companion object {
        const val PENDING_MAX_AGE_MS = 120_000L
    }

    private var pendingNumber: String? = null
    private var pendingSubscriptionId: Int? = null
    private var pendingTime: Long = 0
    private var ringSeen = false
    private var offhookSeen = false

    fun screeningAllowed(number: String?, subscriptionId: Int?, now: Long = System.currentTimeMillis()) {
        pendingNumber = number?.takeIf { it.isNotBlank() }
        pendingSubscriptionId = subscriptionId
        pendingTime = now
        ringSeen = false
        offhookSeen = false
    }

    fun screeningRejected() {
        clearPending()
    }

    private fun clearPending() {
        pendingNumber = null
        pendingSubscriptionId = null
        pendingTime = 0
        ringSeen = false
        offhookSeen = false
    }

    private fun ageOk(now: Long) = now - pendingTime < PENDING_MAX_AGE_MS

    fun onCallStateChanged(state: Int, now: Long = System.currentTimeMillis()): MissedCallSmsRequest? {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                ringSeen = true
                return null
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (ringSeen) {
                    offhookSeen = true
                } else if (pendingNumber != null && ageOk(now)) {
                    offhookSeen = true
                }
                return null
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                val shouldSend = ringSeen && !offhookSeen && pendingNumber != null && ageOk(now)
                val number = pendingNumber
                val subId = pendingSubscriptionId
                clearPending()
                return if (shouldSend && number != null) {
                    MissedCallSmsRequest(number, subId)
                } else {
                    null
                }
            }
            else -> return null
        }
    }
}

data class MissedCallSmsRequest(
    val number: String,
    val subscriptionId: Int?
)

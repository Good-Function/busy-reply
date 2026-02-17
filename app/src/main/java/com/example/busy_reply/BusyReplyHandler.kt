package com.example.busy_reply

import android.net.Uri
import android.telephony.TelephonyManager

/**
 * Result of deciding whether to allow or reject an incoming call
 * when the user may be busy (on another call).
 */
enum class BusyReplyResult {
    ALLOW,
    REJECT
}

/**
 * Handles the decision to send a busy-reply SMS and reject the call,
 * or allow the call. Testable; inject [SmsSender].
 */
class BusyReplyHandler(
    private val smsSender: SmsSender
) {
    /**
     * @param callState [TelephonyManager.CALL_STATE_OFFHOOK] when user is busy
     * @param destinationAddress normalized phone number, or null if unknown
     * @param message saved busy-reply text; if blank, call is allowed
     * @return ALLOW to let the call through, REJECT to reject (after sending SMS if applicable)
     */
    fun handle(
        callState: Int,
        destinationAddress: String?,
        message: String?
    ): BusyReplyResult {
        val isBusy = callState == TelephonyManager.CALL_STATE_OFFHOOK
        val trimmedMessage = message?.trim().orEmpty()
        val hasValidDestination = !destinationAddress.isNullOrBlank()

        if (isBusy && trimmedMessage.isNotEmpty() && hasValidDestination) {
            try {
                smsSender.sendSms(destinationAddress, trimmedMessage)
            } catch (_: Exception) { /* still reject so caller gets busy/voicemail */ }
            return BusyReplyResult.REJECT
        }
        return BusyReplyResult.ALLOW
    }
}

/**
 * Extracts phone number from a telecom handle Uri (e.g. tel:+1234567890).
 * Returns null if handle is null or not a tel: URI.
 */
fun phoneNumberFromHandle(handle: Uri?): String? {
    if (handle == null) return null
    if (handle.scheme?.equals("tel", ignoreCase = true) != true) return null
    return handle.schemeSpecificPart?.takeIf { it.isNotBlank() }
}

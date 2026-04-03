package com.example.busy_reply

import android.net.Uri
import android.telephony.TelephonyManager

enum class BusyReplyResult {
    ALLOW,
    REJECT
}

/** Busy line: optionally SMS + reject; otherwise allow the incoming call. */
class BusyReplyHandler(
    private val smsSender: SmsSender
) {
    fun handle(
        callState: Int,
        destinationAddress: String?,
        message: String?,
        replyWhenBusy: Boolean = true
    ): BusyReplyResult {
        val isBusy = callState == TelephonyManager.CALL_STATE_OFFHOOK
        val trimmedMessage = message?.trim().orEmpty()
        val hasValidDestination = !destinationAddress.isNullOrBlank()

        if (isBusy && replyWhenBusy && trimmedMessage.isNotEmpty() && hasValidDestination) {
            try {
                smsSender.sendSms(destinationAddress, trimmedMessage)
            } catch (_: Exception) { /* still reject so caller gets busy/voicemail */ }
            return BusyReplyResult.REJECT
        }
        return BusyReplyResult.ALLOW
    }
}

fun phoneNumberFromHandle(handle: Uri?): String? {
    if (handle == null) return null
    if (handle.scheme?.equals("tel", ignoreCase = true) != true) return null
    return handle.schemeSpecificPart?.takeIf { it.isNotBlank() }
}

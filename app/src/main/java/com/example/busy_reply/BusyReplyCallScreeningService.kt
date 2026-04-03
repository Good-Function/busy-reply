package com.example.busy_reply

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

class BusyReplyCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val callerNumber = phoneNumberFromHandle(callDetails.handle)
        val callState = (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).callState
        val message = getSharedPreferences(BusyReplyPrefs.PREFS_NAME, MODE_PRIVATE)
            .getString(BusyReplyPrefs.KEY_SAVED_TEXT, null)
        val subscriptionId = subscriptionIdFromCall(callDetails)
        val smsSender = SmsManagerSmsSender.forCall(subscriptionId)
        val result = BusyReplyHandler(smsSender).handle(
            callState,
            callerNumber,
            message
        )
        val shouldReject = result == BusyReplyResult.REJECT
        val response = CallResponse.Builder().apply {
            setDisallowCall(shouldReject)
            setRejectCall(shouldReject)
        }.build()
        respondToCall(callDetails, response)
    }

    private fun subscriptionIdFromCall(callDetails: Call.Details): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) return null
        val accountHandle = callDetails.accountHandle ?: return null
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        return try {
            telephonyManager.getSubscriptionId(accountHandle).takeIf { it >= 0 }
        } catch (_: SecurityException) { null }
        catch (_: Exception) { null }
    }

}

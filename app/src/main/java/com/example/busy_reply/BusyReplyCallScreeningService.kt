package com.example.busy_reply

import android.content.Context
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.CallScreeningService.CallResponse
import android.telephony.TelephonyManager

class BusyReplyCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val callState = (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).callState
        val message = getSharedPreferences(BusyReplyPrefs.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(BusyReplyPrefs.KEY_SAVED_TEXT, null)
        val result = BusyReplyHandler(SmsManagerSmsSender()).handle(
            callState,
            phoneNumberFromHandle(callDetails.handle),
            message
        )
        val shouldReject = result == BusyReplyResult.REJECT
        val response = CallResponse.Builder().apply {
            setDisallowCall(shouldReject)
            setRejectCall(shouldReject)
        }.build()
        respondToCall(callDetails, response)
    }
}

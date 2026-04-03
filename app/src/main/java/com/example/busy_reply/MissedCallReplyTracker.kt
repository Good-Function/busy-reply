package com.example.busy_reply

import android.content.Context

/** Coordinates missed-call SMS: screening supplies the number; [MissedCallMonitorService] supplies call state. */
object MissedCallReplyTracker {

    private val lock = Any()
    private val stateMachine = MissedCallSmsStateMachine()
    private lateinit var appContext: Context

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    fun onScreeningAllowed(number: String?, subscriptionId: Int?) {
        synchronized(lock) {
            stateMachine.screeningAllowed(number, subscriptionId)
        }
    }

    fun onScreeningRejected() {
        synchronized(lock) {
            stateMachine.screeningRejected()
        }
    }

    fun onCallStateChanged(state: Int) {
        val request = synchronized(lock) {
            stateMachine.onCallStateChanged(state)
        } ?: return
        sendMissedReply(request)
    }

    private fun sendMissedReply(request: MissedCallSmsRequest) {
        val prefs = appContext.getSharedPreferences(BusyReplyPrefs.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(BusyReplyPrefs.KEY_REPLY_MISSED_CALL, true)) return
        val message = prefs.getString(BusyReplyPrefs.KEY_SAVED_TEXT, null)?.trim().orEmpty()
        if (message.isEmpty()) return
        try {
            SmsManagerSmsSender.forCall(request.subscriptionId).sendSms(request.number, message)
        } catch (_: Exception) {
        }
    }
}

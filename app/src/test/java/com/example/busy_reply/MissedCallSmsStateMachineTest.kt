package com.example.busy_reply

import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MissedCallSmsStateMachineTest {

    private lateinit var machine: MissedCallSmsStateMachine

    @Before
    fun setUp() {
        machine = MissedCallSmsStateMachine()
    }

    @Test
    fun ringingThenIdleWithoutOffhook_requestsMissedSms() {
        val start = 10_000L
        machine.screeningAllowed("+15550001111", subscriptionId = 1, now = start)
        assertNull(machine.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING, start + 1))
        val req = machine.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE, start + 2)
        assertEquals("+15550001111", req?.number)
        assertEquals(1, req?.subscriptionId)
    }

    @Test
    fun ringingOffhookIdle_doesNotRequestMissedSms() {
        val start = 20_000L
        machine.screeningAllowed("+15550001111", null, now = start)
        machine.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING, start + 1)
        machine.onCallStateChanged(TelephonyManager.CALL_STATE_OFFHOOK, start + 2)
        assertNull(machine.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE, start + 3))
    }

    @Test
    fun idleWithoutRinging_doesNotSend() {
        val start = 30_000L
        machine.screeningAllowed("+15550001111", null, now = start)
        assertNull(machine.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE, start + 1))
    }

    @Test
    fun screeningRejected_clearsPending() {
        val start = 40_000L
        machine.screeningAllowed("+15550001111", null, now = start)
        machine.screeningRejected()
        machine.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING, start + 1)
        assertNull(machine.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE, start + 2))
    }

    @Test
    fun stalePending_notSent() {
        val start = 5_000_000L
        machine.screeningAllowed("+15550001111", null, now = start)
        machine.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING, start + 1)
        assertNull(
            machine.onCallStateChanged(
                TelephonyManager.CALL_STATE_IDLE,
                start + MissedCallSmsStateMachine.PENDING_MAX_AGE_MS + 1
            )
        )
    }
}

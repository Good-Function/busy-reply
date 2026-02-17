package com.example.busy_reply

import android.net.Uri
import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BusyReplyHandlerTest {

    private lateinit var mockSmsSender: MockSmsSender
    private lateinit var handler: BusyReplyHandler

    @Before
    fun setUp() {
        mockSmsSender = MockSmsSender()
        handler = BusyReplyHandler(mockSmsSender)
    }

    @Test
    fun whenBusyAndMessageAndNumber_sendsSmsAndReturnsReject() {
        val result = handler.handle(
            callState = TelephonyManager.CALL_STATE_OFFHOOK,
            destinationAddress = "+15551234567",
            message = "I'll call you back"
        )
        assertEquals(BusyReplyResult.REJECT, result)
        assertEquals(1, mockSmsSender.sent.size)
        assertEquals("+15551234567", mockSmsSender.sent[0].first)
        assertEquals("I'll call you back", mockSmsSender.sent[0].second)
    }

    @Test
    fun whenIdle_returnsAllowAndDoesNotSendSms() {
        val result = handler.handle(
            callState = TelephonyManager.CALL_STATE_IDLE,
            destinationAddress = "+15551234567",
            message = "I'll call you back"
        )
        assertEquals(BusyReplyResult.ALLOW, result)
        assertTrue(mockSmsSender.sent.isEmpty())
    }

    @Test
    fun whenRinging_returnsAllowAndDoesNotSendSms() {
        val result = handler.handle(
            callState = TelephonyManager.CALL_STATE_RINGING,
            destinationAddress = "+15551234567",
            message = "I'll call you back"
        )
        assertEquals(BusyReplyResult.ALLOW, result)
        assertTrue(mockSmsSender.sent.isEmpty())
    }

    @Test
    fun whenBusyButEmptyMessage_returnsAllowAndDoesNotSendSms() {
        val result = handler.handle(
            callState = TelephonyManager.CALL_STATE_OFFHOOK,
            destinationAddress = "+15551234567",
            message = ""
        )
        assertEquals(BusyReplyResult.ALLOW, result)
        assertTrue(mockSmsSender.sent.isEmpty())
    }

    @Test
    fun whenBusyButBlankMessage_returnsAllowAndDoesNotSendSms() {
        val result = handler.handle(
            callState = TelephonyManager.CALL_STATE_OFFHOOK,
            destinationAddress = "+15551234567",
            message = "   "
        )
        assertEquals(BusyReplyResult.ALLOW, result)
        assertTrue(mockSmsSender.sent.isEmpty())
    }

    @Test
    fun whenBusyButNullDestination_returnsAllowAndDoesNotSendSms() {
        val result = handler.handle(
            callState = TelephonyManager.CALL_STATE_OFFHOOK,
            destinationAddress = null,
            message = "I'll call you back"
        )
        assertEquals(BusyReplyResult.ALLOW, result)
        assertTrue(mockSmsSender.sent.isEmpty())
    }

    @Test
    fun whenBusyButBlankDestination_returnsAllowAndDoesNotSendSms() {
        val result = handler.handle(
            callState = TelephonyManager.CALL_STATE_OFFHOOK,
            destinationAddress = "",
            message = "I'll call you back"
        )
        assertEquals(BusyReplyResult.ALLOW, result)
        assertTrue(mockSmsSender.sent.isEmpty())
    }

    @Test
    fun whenBusyAndNullMessage_returnsAllowAndDoesNotSendSms() {
        val result = handler.handle(
            callState = TelephonyManager.CALL_STATE_OFFHOOK,
            destinationAddress = "+15551234567",
            message = null
        )
        assertEquals(BusyReplyResult.ALLOW, result)
        assertTrue(mockSmsSender.sent.isEmpty())
    }

    private class MockSmsSender : SmsSender {
        val sent = mutableListOf<Pair<String, String>>()
        override fun sendSms(destinationAddress: String, message: String) {
            sent.add(destinationAddress to message)
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PhoneNumberFromHandleTest {

    @Test
    fun telUri_returnsSchemeSpecificPart() {
        val uri = Uri.parse("tel:+15551234567")
        assertEquals("+15551234567", phoneNumberFromHandle(uri))
    }

    @Test
    fun nullHandle_returnsNull() {
        assertNull(phoneNumberFromHandle(null))
    }

    @Test
    fun nonTelScheme_returnsNull() {
        assertNull(phoneNumberFromHandle(Uri.parse("sip:user@host")))
    }

    @Test
    fun telUriWithSpaces_returnsPart() {
        val uri = Uri.parse("tel:+1 555 123 4567")
        assertEquals("+1 555 123 4567", phoneNumberFromHandle(uri))
    }
}

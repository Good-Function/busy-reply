package com.example.busy_reply

/**
 * Abstraction for sending SMS so it can be mocked in tests.
 */
interface SmsSender {
    fun sendSms(destinationAddress: String, message: String)
}

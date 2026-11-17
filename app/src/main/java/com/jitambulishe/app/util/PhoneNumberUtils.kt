package com.jitambulishe.app.util

import android.telephony.PhoneNumberUtils
import java.util.Locale

object PhoneNumberUtils {

    /**
     * Normalize a raw caller number to E.164 form where possible.
     * Kenya country code is 254, so a bare "0712345678" becomes "+254712345678".
     */
    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return raw ?: ""
        val trimmed = raw.trim()
        if (trimmed.startsWith("+")) return trimmed

        // Bare local number: strip leading 0 and prefix +254
        val digits = trimmed.filter { it.isDigit() }
        return when {
            digits.startsWith("0") -> "+254" + digits.substring(1)
            digits.startsWith("254") -> "+$digits"
            else -> "+$digits"
        }
    }

    fun formatForDisplay(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown"
        return try {
            PhoneNumberUtils.formatNumber(raw, Locale.getDefault().country)
        } catch (_: Exception) {
            raw
        }
    }
}

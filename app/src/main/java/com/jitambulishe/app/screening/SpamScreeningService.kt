package com.jitambulishe.app.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import com.jitambulishe.app.data.AppDatabase
import com.jitambulishe.app.util.PhoneNumberUtils
import kotlinx.coroutines.runBlocking

/**
 * The heart of Jitambulishe. Called by Android for every incoming call when the
 * app holds the ROLE_CALL_SCREENING role.
 *
 * CRITICAL: this method must return *fast*. It runs on a system thread with a
 * short deadline; blocking it on a network call or a slow disk read will cause
 * the call to ring late or not at all. So we deliberately use runBlocking only
 * around a *tiny* Room query (which is cached and fast) and nothing else.
 */
class SpamScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val rawNumber = callDetails.handle?.schemeSpecificPart
        val number = PhoneNumberUtils.normalize(rawNumber)
        val response = CallResponse.Builder()

        if (number.isBlank()) {
            // No number (private/unknown). Let the system decide; don't block.
            respondToCall(callDetails, response.setDisallowCall(false).build())
            return
        }

        val dao = AppDatabase.get(this).spamDao()

        // 1. Local blocklist — fast, cached
        val isBlocked = runBlocking { dao.isSpam(number) }
        if (isBlocked) {
            response.setDisallowCall(true).setRejectCall(true)
            respondToCall(callDetails, response.build())
            return
        }

        // 2. STIR/SHAKEN — instant carrier verdict
        if (callDetails.callerNumberVerificationStatus ==
            Connection.VERIFICATION_STATUS_FAILED
        ) {
            // Spoofed or invalid number — treat as spam
            response.setDisallowCall(true).setRejectCall(true)
            respondToCall(callDetails, response.build())
            return
        }

        // 3. Unknown — allow to ring. (Truecaller lookup happens out-of-band;
        //    the app never blocks the call path on it.)
        response.setDisallowCall(false)
        respondToCall(callDetails, response.build())
    }
}

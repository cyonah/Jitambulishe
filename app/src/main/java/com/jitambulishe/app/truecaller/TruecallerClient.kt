package com.jitambulishe.app.truecaller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Placeholder client for a Truecaller lookup API.
 *
 * IMPORTANT: You said you'll fill this in later. The contract the rest of the
 * app expects is simple:
 *
 *   suspend fun lookup(number: String): TruecallerResult
 *
 * Return either [TruecallerResult.Found] or [TruecallerResult.NotFound].
 * Throw an exception (or return NotFound) on network failure — the caller
 * treats both the same.
 *
 * When you wire up your endpoint, replace [stubLookup] with a real HTTP call
 * using [client]. Example:
 *
 *   val req = Request.Builder()
 *       .url("https://your-api.example.com/lookup?number=$number")
 *       .addHeader("Authorization", "Bearer <token>")
 *       .build()
 *   val body = client.newCall(req).execute().body?.string().orEmpty()
 *   // parse JSON and return Found(name, isSpam) or NotFound
 */
class TruecallerClient(
    private val baseUrl: String? = null,
    private val apiKey: String? = null
) {
    private val client = OkHttpClient()

    suspend fun lookup(number: String): TruecallerResult = withContext(Dispatchers.IO) {
        // If no endpoint is configured, behave gracefully as "not found".
        if (baseUrl.isNullOrBlank()) return@withContext TruecallerResult.NotFound

        // TODO: replace this with a real request once you have an endpoint.
        stubLookup(number)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun stubLookup(number: String): TruecallerResult {
        // A real implementation would issue an HTTP request here.
        return TruecallerResult.NotFound
    }
}

sealed interface TruecallerResult {
    data class Found(val name: String, val isSpam: Boolean) : TruecallerResult
    data object NotFound : TruecallerResult
}

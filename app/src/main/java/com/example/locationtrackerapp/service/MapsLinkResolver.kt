package com.example.locationtrackerapp.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

/**
 * Resolves a Google Maps link into a latitude/longitude pair (plus a
 * best-effort place name), so the user can add a saved location just by
 * pasting a link shared from the Google Maps app.
 *
 * Shortened links like "https://maps.app.goo.gl/..." don't contain the
 * coordinates themselves — they only appear after Google's servers redirect
 * to the full "https://www.google.com/maps/place/.../@lat,lng,zoom/..." URL.
 * Since this app has no Maps/Places API key configured, this resolver
 * follows that redirect chain itself (a handful of plain HTTP 3xx hops) and
 * then reads the coordinates straight out of the resulting URL, which is
 * enough for the URL formats Google Maps actually shares.
 */
object MapsLinkResolver {

    /** Result of resolving a maps link or raw coordinate string. */
    data class Resolved(
        val latitude: Double,
        val longitude: Double,
        val placeName: String? = null
    )

    // Ordered from most to least precise. Google Maps URLs commonly contain
    // several of these at once (e.g. an approximate "@lat,lng" map-center
    // AND a precise "!3d..!4d.." pin) so the precise ones are checked first.
    private val COORD_PATTERNS = listOf(
        Regex("""!3d(-?\d+\.\d+)!4d(-?\d+\.\d+)"""),           // exact pin: ...!3d48.85!4d2.29...
        Regex("""[?&]q=(-?\d+\.\d+),(-?\d+\.\d+)"""),          // dropped pin: ?q=48.85,2.29
        Regex("""/maps/dir/[^/]*/(-?\d+\.\d+),(-?\d+\.\d+)"""), // directions destination
        Regex("""@(-?\d+\.\d+),(-?\d+\.\d+)""")                // map center: /@48.85,2.29,17z
    )

    private val PLACE_NAME_PATTERN = Regex("""/maps/place/([^/@]+)""")
    private val RAW_COORDS_PATTERN = Regex("""^(-?\d+(?:\.\d+)?)\s*,\s*(-?\d+(?:\.\d+)?)$""")

    /**
     * Resolves [rawInput] into coordinates. Accepts a full or shortened
     * Google Maps URL, or a plain "lat,lng" pair typed/pasted directly.
     * Returns null if no coordinates could be determined.
     */
    suspend fun resolve(rawInput: String): Resolved? = withContext(Dispatchers.IO) {
        val input = rawInput.trim()
        if (input.isEmpty()) return@withContext null

        parseRawCoordinates(input)?.let { return@withContext it }

        if (!input.startsWith("http://") && !input.startsWith("https://")) {
            return@withContext null
        }

        val finalUrl = try {
            followRedirects(input)
        } catch (e: Exception) {
            return@withContext null
        }

        extractFromUrl(finalUrl)
    }

    private fun parseRawCoordinates(input: String): Resolved? {
        val match = RAW_COORDS_PATTERN.find(input) ?: return null
        val lat = match.groupValues[1].toDoubleOrNull() ?: return null
        val lng = match.groupValues[2].toDoubleOrNull() ?: return null
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return null
        return Resolved(lat, lng)
    }

    /**
     * Follows HTTP redirects manually (rather than letting HttpURLConnection
     * auto-follow them) so we always end up with the final URL string to
     * run our coordinate regexes against.
     */
    private fun followRedirects(startUrl: String, maxHops: Int = 8): String {
        var currentUrl = startUrl
        repeat(maxHops) {
            val connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
            }
            try {
                val code = connection.responseCode
                val location = connection.getHeaderField("Location")
                currentUrl = if (code in 300..399 && location != null) {
                    if (location.startsWith("http")) location else URL(URL(currentUrl), location).toString()
                } else {
                    return currentUrl
                }
            } finally {
                connection.disconnect()
            }
        }
        return currentUrl
    }

    private fun extractFromUrl(url: String): Resolved? {
        val decoded = try {
            URLDecoder.decode(url, "UTF-8")
        } catch (e: Exception) {
            url
        }

        for (pattern in COORD_PATTERNS) {
            val match = pattern.find(url) ?: pattern.find(decoded)
            if (match != null) {
                val lat = match.groupValues[1].toDoubleOrNull()
                val lng = match.groupValues[2].toDoubleOrNull()
                if (lat != null && lng != null) {
                    return Resolved(lat, lng, extractPlaceName(decoded))
                }
            }
        }
        return null
    }

    private fun extractPlaceName(decodedUrl: String): String? {
        val match = PLACE_NAME_PATTERN.find(decodedUrl) ?: return null
        return match.groupValues[1].replace('+', ' ').trim().takeIf { it.isNotEmpty() }
    }
}

package com.netraze.app.data.wifi

object WifiUtils {

    /**
     * Converts a Wi-Fi frequency (MHz) into its standard Wi-Fi channel number.
     * Supports 2.4 GHz, 5 GHz, and 6 GHz bands.
     */
    fun frequencyToChannel(freqMhz: Int): Int? {
        return when {
            freqMhz in 2412..2472 -> (freqMhz - 2407) / 5
            freqMhz == 2484 -> 14
            freqMhz in 5180..5885 -> (freqMhz - 5000) / 5
            freqMhz in 5955..7115 -> (freqMhz - 5950) / 5
            else -> null
        }
    }

    /**
     * Normalizes BSSID address to standard uppercase format AA:BB:CC:DD:EE:FF.
     */
    fun normalizeBssid(bssid: String): String {
        return bssid.trim().uppercase()
    }
}

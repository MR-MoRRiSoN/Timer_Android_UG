package com.example.timer_android_ug.service


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ntp.NTPUDPClient
import org.apache.commons.net.ntp.TimeInfo
import java.net.InetAddress

object NTPTime {

    private const val NTP_SERVER = "pool.ntp.org"

    fun getTimeInMillis(onResult: (Long) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val timeInMillis = withContext(Dispatchers.IO) { fetchTimeInMillis() }
            onResult(timeInMillis)
        }
    }

    private suspend fun fetchTimeInMillis(): Long {
        val ntpClient = NTPUDPClient()
        val inetAddress = InetAddress.getByName(NTP_SERVER)
        val timeInfo: TimeInfo = ntpClient.getTime(inetAddress)
        timeInfo.computeDetails()
        return System.currentTimeMillis() + timeInfo.offset
    }
}

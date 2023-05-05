package com.example.timer_android_ug

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException
import java.util.Collections
import java.util.Locale
import java.util.concurrent.TimeUnit


class TimeCounter : AppCompatActivity() {
    private lateinit var timerTextView: TextView
    private lateinit var ipAddress: TextView
    private lateinit var roomId: TextView
    private var server: NettyApplicationEngine? = null
    private var isRunning = true
    private val port = 1104

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_counter)
        val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
        val room = sharedPreferences.getString("roomID", "")
        ipAddress = findViewById(R.id.ipAddress)
        roomId = findViewById(R.id.getRoom)
        ipAddress.text = getLocalIPAddress(this) + ":$port"
        roomId.text = "RoomId : $room"
        val endTime = sharedPreferences.getLong("endTime", 0)
        Log.i("remainder", "first end time $endTime")
        if (endTime != 0L) {
            sharedPreferences.edit().putBoolean("TimeStarted", true).apply()
            startTimer()
        }else{
            sharedPreferences.edit().putBoolean("TimeStarted", false).apply()

        }
        GlobalScope.launch(Dispatchers.IO) {
            startWebServer()
        }

    }

    private fun startWebServer() {
        val checkIpAddress = getLocalIPAddress(this).toString()
        val checkPort = isPortOpen(checkIpAddress, port, 0)
        if (!checkPort) {
            server = embeddedServer(Netty, port = port) {
                routing {
                    post("/start/{time}") {
                        val command = call.parameters["command"] ?: ""
                        val time = call.parameters["time"]
                            val pref = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
                            pref.edit().putLong("endTime", time!!.toLong()).apply()
                            val endTime = pref.getLong("endTime", 0)
                            val checkStartTime = pref.getBoolean("TimeStarted", false)
                            Log.i("--------------++++++++++++", "first end time $checkStartTime")

                            if (endTime != 0L && !checkStartTime) {
                                isRunning=true
                                Log.i("--------------++++++++++++", "first end time $checkStartTime")
                                Thread {
                                    startTimer()
                                }.start()
                            }
                        call.respondText("Received: start", ContentType.Text.Plain)
                    }
                }
                routing {
                    post("/reset") {
                        resetTime()
                        call.respondText("Received: Rested", ContentType.Text.Plain)
                    }
                }
                routing {
                    post("/pause") {
                        resetTime()
                        call.respondText("Received: Pause", ContentType.Text.Plain)
                    }
                }
                routing {
                    post("/Resume") {
                        resetTime()
                        call.respondText("Received: Resume", ContentType.Text.Plain)
                    }
                }
            }
            server!!.start(wait = false)
        } else {
            println("Server already running")
        }
    }

    private fun isPortOpen(host: String, port: Int, timeout: Int): Boolean {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeout)
                return true
            }
        } catch (e: IOException) {
            // Port is closed or an error occurred
            return false
        }
    }

    private fun stopWebServer() {
        if (server != null) {
            server!!.stop(0, 0)
            server = null
        } else {
            println("Server is not running")
        }
    }


    private fun startTimer() {
        runOnUiThread {
            timerTextView = findViewById(R.id.timerTextView)
            val currentSeconds = System.currentTimeMillis()
            val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
            val endTime = sharedPreferences.getLong("endTime", 0)
            val remainingTime = endTime - currentSeconds

            val countDownTimer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (!isRunning) {
                        timerTextView.text = "00:00:00"
                        cancel()
                        return
                    }

                    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                    timerTextView.text = String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d",
                        hours,
                        minutes,
                        seconds
                    )
                }

                override fun onFinish() {
                    if (!isRunning) {
                        return
                    }

                    timerTextView.text = "00:00:00"
                }
            }
            countDownTimer.start()
        }
    }


    private fun getLocalIPAddress(context: Context): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                )
            ) {
                return getLocalWifiOrEthernetIPAddress()
            }
        }
        return null
    }

    private fun getLocalWifiOrEthernetIPAddress(): String? {
        try {
            val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in networkInterfaces) {
                val inetAddresses = Collections.list(networkInterface.inetAddresses)
                for (inetAddress in inetAddresses) {
                    if (!inetAddress.isLoopbackAddress && inetAddress.isSiteLocalAddress) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }

    private fun resetTime() {
        val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong("endTime", 0L).apply()
        sharedPreferences.edit().putBoolean("TimeStarted", false).apply()
        isRunning = false



    }
}

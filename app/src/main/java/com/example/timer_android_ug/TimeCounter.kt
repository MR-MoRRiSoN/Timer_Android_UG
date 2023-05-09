package com.example.timer_android_ug

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timer_android_ug.service.NTPTime
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException
import java.util.Collections
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.os.Process


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
        initialisePage()
        GlobalScope.launch(Dispatchers.IO) {
            startWebServer()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun initialisePage(){
        val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
        val room = sharedPreferences.getString("roomID", "NULL")
        ipAddress = findViewById(R.id.ipAddress)
        roomId = findViewById(R.id.getRoom)
        ipAddress.text = getLocalIPAddress(this) + ":$port"
        roomId.text = "RoomId : $room"
        val endTime = sharedPreferences.getLong("endTime", 0L)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (endTime > 0L) {
            Log.i("Start", "1")
            sharedPreferences.edit().putBoolean("TimeStarted", true).apply()
            startTimer()
        } else {
            Log.i("Start", "2")
            resetTime()

        }
    }

    private fun setRoomId(){
        val intent = Intent(applicationContext, TimeCounter::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
         isRunning = true
    }

    private fun startWebServer() {
        val checkIpAddress = getLocalIPAddress(this).toString()
        val checkPort = isPortOpen(checkIpAddress, port, 0)
        if (!checkPort) {
            server = embeddedServer(Netty, port = port) {
                routing {
                    post("/registerDevice/{roomId}") {
                        val httpRoomId = call.parameters["roomId"]
                        val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
                        if (sharedPreferences.getString("roomID","NULL")=="NULL"){
                            sharedPreferences.edit().putString("roomID", httpRoomId).apply()
                            call.respondText("Received: Device Registered", ContentType.Text.Plain)
                            setRoomId()
                        }else{
                            call.respondText("Received: Already Registered", ContentType.Text.Plain)
                        }


                    }
                }
                routing {
                    post("/cleanDevice") {
                        val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putString("roomID", "NULL").apply()
                        roomId = findViewById(R.id.getRoom)
                        call.respondText("Received: Device Cleaned", ContentType.Text.Plain)
                        setRoomId()
                    }
                }
                routing {
                    post("/start/{time}") {
                        val command = call.parameters["command"] ?: ""
                        val time = call.parameters["time"]
                        val pref = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
                        pref.edit().putLong("endTime", time!!.toLong()).apply()
                        val checkStartTime = pref.getBoolean("TimeStarted", false)
                        Log.i("--------------++++++++++++", "first end time $checkStartTime")

                        if (!checkStartTime) {
                            isRunning = true
                            Log.i("--------------++++++++++++", "first end time $checkStartTime")
                            Thread {
                                startTimer()
                            }.start()
                            pref.edit().putBoolean("TimeStarted", true).apply()
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
                    post("/addTime") {
                        resetTime()
                        call.respondText("Received: Pause", ContentType.Text.Plain)
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
            NTPTime.getTimeInMillis { timeInMillis ->

                val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
                val endTime = sharedPreferences.getLong("endTime", 0)
                val remainingTime = endTime - timeInMillis

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
                    resetTime()
                        timerTextView.text = "00:00:00"
                    }


                }
                countDownTimer.start()
            }

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

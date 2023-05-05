package com.example.timer_android_ug

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.timer_android_ug.service.CountdownViewModel
import com.example.timer_android_ug.service.CountdownViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var countdownTextView: TextView
    private lateinit var roomId: TextView
    private lateinit var textViewCardData: TextView

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>
    private lateinit var techLists: Array<Array<String>>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        roomId=findViewById(R.id.getRoom)
         val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
         val room = sharedPreferences.getString("roomID", "")
//         val countdownViewModel: CountdownViewModel by viewModels { CountdownViewModelFactory(room.toString()) }
//        roomId.text="RoomId : $room"
//        countdownTextView = findViewById(R.id.countdownTextView)

//
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
//        pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
//            PendingIntent.FLAG_MUTABLE
//        )

//        intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
//        techLists = arrayOf(
//            arrayOf(MifareClassic::class.java.name),
//            arrayOf(NfcA::class.java.name)
//        )
//
//
//        countdownViewModel.remainingTime.observe(this, Observer { timeText ->
//            countdownTextView.text = timeText
//        })
//    }

//
//    override fun onResume() {
//        super.onResume()
//        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        nfcAdapter.disableForegroundDispatch(this)
//    }
//
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//
//        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
//            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
//            val mifareClassic = MifareClassic.get(tag)
//
//            if (mifareClassic != null) {
//                try {
//                    mifareClassic.connect()
//
//                    // Extract card ID (UID) from the Tag
//                    val cardIdBytes = tag!!.id
//                    val cardId = cardIdBytes.joinToString(separator = "") { String.format("%02X", it) }
//                    Toast.makeText(this, "Card ID: $cardId", Toast.LENGTH_LONG).show()
//
//                } catch (e: Exception) {
//                    Toast.makeText(this, "Error reading card: ${e.message}", Toast.LENGTH_LONG).show()
//                } finally {
//                    mifareClassic.close()
//                }
//            }
//        }
//    }


}
}

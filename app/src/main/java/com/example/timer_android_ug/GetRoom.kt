package com.example.timer_android_ug

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText


class GetRoom : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_room)
        if (checkRoomId()){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }else{
            saveRoom()
        }
    }
    private fun saveRoom(){
        val button = findViewById<Button>(R.id.submitRoom)
        val roomId = findViewById<EditText>(R.id.getRoom)
        val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)


        button.setOnClickListener {
            sharedPreferences.edit().putString("roomID", roomId.text.toString()).apply()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private  fun  checkRoomId():Boolean{
        val sharedPreferences = getSharedPreferences("roomID_pref", Context.MODE_PRIVATE)
        val text = sharedPreferences.getString("roomID", "")
        return text!="";
    }
    override fun onBackPressed() {
    }
}

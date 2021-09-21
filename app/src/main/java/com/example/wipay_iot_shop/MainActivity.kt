package com.example.wipay_iot_shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.testpos.database.transaction.AppDatabase
import com.example.testpos.database.transaction.ReversalDao
import com.example.testpos.database.transaction.SaleDao

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_SignIn = findViewById(com.example.wipay_iot_shop.R.id.btn_SignIn) as Button
        val et_Email = findViewById<EditText>(R.id.et_Email)
        val et_Password = findViewById<EditText>(R.id.et_Password)
        btn_SignIn.setOnClickListener {
            val email = et_Email.text.toString()
            val password = et_Password.text.toString()
            if (email != "wipay"&&password != "1234") {
                startActivity(Intent(this,MenuActivity::class.java))
            }else{
                Toast.makeText(this, "Invalid password or username", Toast.LENGTH_SHORT).show()
                et_Email.setText("")
                et_Password.setText("")
            }
            Log.i("test","email :"+email+"\t password :"+password)
        }
    }



}
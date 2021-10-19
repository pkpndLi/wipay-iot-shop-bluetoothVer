package com.example.wipay_iot_shop

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.graphics.drawable.Drawable
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import org.jetbrains.annotations.NotNull
import java.lang.RuntimeException
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class MenuActivity : AppCompatActivity() {

    private lateinit var sp: SharedPreferences

    var totalAmount:Int? = null
    var cardNO:String = ""
    var cardEXD:String = ""
    var menuName:String = ""
    var from:String = ""
//    var settlementFlag:Boolean? = null
//    var firstTransactionFlag:Boolean? = null
//    var startId:Int = 1

    private val MY_PREFS = "my_prefs"
    var stringValue = ""
    var booleanValue : Boolean? = null
    var log = "log2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)

//        val editor: SharedPreferences.Editor = sp.edit()
////        editor.putString("stringKey", "This is a book!"
//        editor.putBoolean("settlementFlag", false)
//        editor.putBoolean("firstTransactionFlag", true)
//        editor.putInt("startId", 1)
//        editor.commit()

        val listImg = findViewById<ImageView>(R.id.list)
        val goodsImg1 = findViewById<ImageView>(R.id.goods1)
        val goodsImg2 = findViewById<ImageView>(R.id.goods2)
        val goodsImg3 = findViewById<ImageView>(R.id.goods3)

        listImg.setImageDrawable(getImage(this, "list96"))
        goodsImg1.setImageDrawable(getImage(this, "coffee"))
        goodsImg2.setImageDrawable(getImage(this, "coffee1"))
        goodsImg3.setImageDrawable(getImage(this, "brownie"))




        intent.apply {
//          processing = getBooleanExtra("processing",false)
            totalAmount = getIntExtra("totalAmount",145)
            menuName = getStringExtra("menuName").toString()
//            from = getStringExtra("from").toString()
        }



        goodsImg1.setOnClickListener{

            val itn =Intent(this,InfoActivity::class.java).apply{
                putExtra("menu","goods1")
                putExtra("amount",145)
            }
            startActivity(itn)
        }



        goodsImg2.setOnClickListener{
            val itn =Intent(this,InfoActivity::class.java).apply{
                putExtra("menu","goods2")
                putExtra("amount",145)
            }
            startActivity(itn)
        }

        goodsImg3.setOnClickListener{
            val itn =Intent(this,InfoActivity::class.java).apply{
                putExtra("menu","goods3")
                putExtra("amount",120)
            }
            startActivity(itn)
        }

        listImg.setOnClickListener {
            startActivity(Intent(this,SettlementActivity::class.java))
        }


//test sharedPreferences
//        stringValue = sp.getString("stringKey","not found!").toString()
//        booleanValue = sp.getBoolean("booleanKey",false)
//
//        Log.i(log, "String value: " + stringValue)
//        Log.i(log, "Boolean value: " + booleanValue)



    }

    fun getImage(context: Context, name: String?): Drawable? {
        return context.getResources().getDrawable(
            context.getResources().getIdentifier(name, "drawable", context.getPackageName())
        )
    }


}
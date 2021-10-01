package com.example.wipay_iot_shop

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.graphics.drawable.Drawable
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import java.lang.RuntimeException
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class MenuActivity : AppCompatActivity() {

    var totalAmount:Int? = null
    var cardNO:String = ""
    var cardEXD:String = ""
    var menuName:String = ""
    var from:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

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

//        if(from == "transAct"){
//            sendEmailProcess()
//        }
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)

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
            startActivity(Intent(this,SettingActivity::class.java))
        }


    }

    fun getImage(context: Context, name: String?): Drawable? {
        return context.getResources().getDrawable(
            context.getResources().getIdentifier(name, "drawable", context.getPackageName())
        )
    }

    fun sendEmailProcess(){

        Log.i("log_tag","send email.")
        //Send Email Slip
        val _txtEmail = "phanida.lip@gmail.com"
        val username = "phanida601@gmail.com"
        val password = "1469900351198"
        val messageToSend = "test send eamil wipay shop."
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })
        try {
            val message: Message = MimeMessage(session)
            message.setFrom(InternetAddress(username))
            message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(_txtEmail)
            )
            message.subject = "Sending email without opening gmail apps"
            message.setText(messageToSend)
            Transport.send(message)
            Toast.makeText(
                applicationContext,
                "email send successfully.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: MessagingException) {
            throw RuntimeException(e)
        }


    }
}
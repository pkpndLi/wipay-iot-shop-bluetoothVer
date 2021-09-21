package com.example.wipay_iot_shop

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.graphics.drawable.Drawable




class MenuActivity : AppCompatActivity() {


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
}
package com.example.wipay_iot_shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val merchantList = findViewById<ListView>(R.id.merchantList)

        val Items = ArrayList<Model>()
        Items.add(Model("Merchant Location","-",R.drawable.location64))
        Items.add(Model("Merchant ID","222222222222222",R.drawable.shop64))
        Items.add(Model("Terminal ID","22222222",R.drawable.terminal64))
        Items.add(Model("Settlement Transaction","",R.drawable.summary))
        Items.add(Model("Initial Config","",R.drawable.start))

        val adapter = SettingAdapter(this,R.layout.merchantlist,Items)
        merchantList.adapter = adapter
    }
}
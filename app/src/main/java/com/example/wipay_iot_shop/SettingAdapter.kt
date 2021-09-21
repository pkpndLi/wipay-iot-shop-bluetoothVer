package com.example.wipay_iot_shop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class SettingAdapter (var ctx: Context, var ressource: Int, var Item: ArrayList<Model>): ArrayAdapter<Model>(ctx,ressource,Item){

    override fun getView(position: Int, convertViwe: View?, parent: ViewGroup): View {

        val layoutInflater = LayoutInflater.from(ctx)
        val view = layoutInflater.inflate(ressource, null)


        val topicName = view.findViewById<TextView>(R.id.topic)
        val detailsName = view.findViewById<TextView>(R.id.details)
        val img = view.findViewById<ImageView>(R.id.merchLo)

        topicName.text = Item[position].topicName
        detailsName.text = Item[position].detailsName
        img.setImageDrawable(ctx.resources.getDrawable(Item[position].Img))

        return view



    }


}
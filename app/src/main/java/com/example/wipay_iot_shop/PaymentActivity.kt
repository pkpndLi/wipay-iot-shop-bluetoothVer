package com.example.wipay_iot_shop

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class PaymentActivity : AppCompatActivity() ,View.OnClickListener{


    lateinit var btn_SelectMag : Button
    lateinit var btn_SelectEMV : Button
    lateinit var btn_QR : Button
    lateinit var btn_SelectOK : Button
    lateinit var tv_InfoPayment : TextView
    var Status = -1
    var mCardType = -1
    private val bIsBack = false
    private var m_bThreadFinished = true

    var totalAmount:Int? = null
    var menuName: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )


            intent.apply {
                totalAmount = getIntExtra("totalAmount",145)
                menuName = getStringExtra("menuName").toString()
            }

            Toast.makeText(applicationContext,"totalAmount" + totalAmount,Toast.LENGTH_LONG).show()


            setView()


    }

    fun setView(){
        //textview
        tv_InfoPayment = findViewById(R.id.tv_InfoPayment)
        //buttom
        btn_SelectMag = findViewById(R.id.btn_SelectMag)
        btn_SelectEMV = findViewById(R.id.btn_SelectEMV)
        btn_QR = findViewById(R.id.btn_QR)
        btn_SelectOK = findViewById(R.id.btn_SelectOK)
        btn_SelectMag.setOnClickListener(this)
        btn_SelectEMV.setOnClickListener(this)
        btn_QR.setOnClickListener(this)
        btn_SelectOK.setOnClickListener(this)
    }

    interface IBackFinish {
        fun isBack()
    }

    var mIBackFinish: IBackFinish? = null

    fun setIBackFinish(mIBackFinish: IBackFinish) {
        this.mIBackFinish = mIBackFinish
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btn_SelectMag->{
                try {
                    btn_SelectMag.isEnabled = false
                    btn_SelectEMV.isEnabled = true
                    btn_QR.isEnabled = true

                    val itn =Intent(this,ManageBluetooth::class.java).apply{
                        putExtra("totalAmount",totalAmount)
                        putExtra("menuName",menuName)
                        putExtra("scanMethod","swipe")
                    }
                    startActivity(itn)

                }catch (e: Exception){

                }
            }

            R.id.btn_SelectEMV->{
                try {
                    btn_SelectEMV.isEnabled = false
                    btn_SelectMag.isEnabled = true
                    btn_QR.isEnabled = true

                    val itn =Intent(this,ManageBluetooth::class.java).apply{
                        putExtra("totalAmount",totalAmount)
                        putExtra("menuName",menuName)
                        putExtra("scanMethod","insert")
                    }
                    startActivity(itn)

                }catch (e: Exception){

                }

            }
            R.id.btn_QR->{
                try {
                    btn_QR.isEnabled = false
                    btn_SelectMag.isEnabled = true
                    btn_SelectEMV.isEnabled = true

                }catch (e: Exception){

                }
            }
            R.id.btn_SelectOK->{
                try{

                }catch (e: Exception){

                }

            }
        }
    }

}

package com.example.wipay_iot_shop

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.AppDatabase
import com.example.testpos.database.transaction.SaleDao
import com.example.testpos.database.transaction.SaleEntity
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.transaction.FlagReverseEntity
import com.example.wipay_iot_shop.transaction.ResponseDao
import com.example.wipay_iot_shop.transaction.ResponseEntity
import com.example.wipay_iot_shop.transaction.StuckReverseEntity
import com.imohsenb.ISO8583.builders.ISOClientBuilder
import com.imohsenb.ISO8583.builders.ISOMessageBuilder
import com.imohsenb.ISO8583.entities.ISOMessage
import com.imohsenb.ISO8583.enums.FIELDS
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN
import com.imohsenb.ISO8583.enums.VERSION
import com.imohsenb.ISO8583.exceptions.ISOClientException
import com.imohsenb.ISO8583.exceptions.ISOException
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice
import github.nisrulz.screenshott.ScreenShott
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList
import kotlin.experimental.and

class SettlementActivity : AppCompatActivity() {

    var appDatabase : AppDatabase? = null
    var saleDAO : SaleDao? = null
    var responseDAO : ResponseDao? = null

    // Get SharedPreferences
    private val MY_PREFS = "my_prefs"
    private lateinit var sp: SharedPreferences

    var saleCountTxt: TextView? = null
    var saleAmountTxt: TextView? = null

    var stringValue = ""
    var booleanValue : Boolean? = null
    var log = "log"

    var readStan: Int? = null
    var stan: Int? = null
    var readId: Int? = null
    var isoMsgList = ArrayList<String>()
    var newIsoMsgList = ArrayList<String>()
    var isoMsg: String? = null
    var readIsoMsg: String? = null
    var readResponseMsg:String? = null
    var responseMsg:String? = null

    var saleCount: Int? = 0
    var saleAmount: Int? = 0
    var responseCount: Int? = 0
    var responseAmount: Int? = 0
    var batchTotals: String? = null


    var settlementFirstPccCode: String = "920000"
    var settlementLastPccCode: String = "960000"
    var amount:String = ""
    var cardNO:String = ""
    var cardEXD:String = ""
    var MTI:String = ""
    var oldStan:String = ""
    var TID: String = "3232323232323232"
    var MID: String = "323232323232323232323232323232"
    var batchNumber: String = "000142"

    var responseCode: String? = null

    var batchResponseFlag: Int? = 1
    var batchUploadLoopFlag: Boolean? = null

    private var saleReport: View? = null
    private var bitmap: Bitmap? = null
    private val RC_WRITE_EXTERNAL_STORAGE = 123

    //    private val HOST = "192.168.43.195"
//    var PORT = 5000
//    private val HOST = "192.168.68.195"
//    private val HOST = "192.168.68.225"
//    var PORT = 5000
//    private val HOST = "192.168.43.24"
//    var PORT = 3000
//    private val HOST = "192.168.68.119"
//    var PORT = 5001
//    private val HOST = "203.148.160.47"
//    var PORT = 7500

    private val HOST = "192.168.178.187"
    var PORT = 5000

//    private val HOST = "192.168.68.107"
//    var PORT = 3000

    var settlementFlag:Boolean? = null
    var firstTransactionFlag:Boolean? = null
    var oldStartId:Int? = null
    var startId:Int? = null
    var endId: Int? = null
    var lastSettlementFlag: Boolean? = null
    var batchStan: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlement)

        Log.d(log,"on settlementActivity.")
        saleReport = findViewById(R.id.SaleReportActivity)

        var confirmBtn = findViewById<Button>(R.id.confirmBtn)
         saleCountTxt = findViewById<TextView>(R.id.saleCountTxt)
         saleAmountTxt = findViewById<TextView>(R.id.sumAmountTxt)

        var batchBtn = findViewById<Button>(R.id.batchBtn)

        intent.apply {
            lastSettlementFlag = getBooleanExtra("lastSettlementFlag",false)
            batchStan = getIntExtra("batchStan",1)
//
        }

        Log.i(log,"lastSettlementFlag: " + lastSettlementFlag)

         sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
         startId = sp.getInt("startId",1)
         oldStartId = sp.getInt("oldStartId",0)
         Log.w(log,"oldStartId: " + oldStartId)
         Log.w(log,"startId: " + startId)


        if(oldStartId == startId){
            setDialog("Processing failed.","There has never been any transaction.")//
        }else if(lastSettlementFlag == true){

            saleCountTxt?.setText(sp.getString("saleCount","saleCount"))
            saleAmountTxt?.setText(sp.getString("saleAmount","saleAmount"))

              Log.w(log,"endId" + endId)
              Log.i(log,"In LastSettlement path")

              stan = batchStan?.plus(1)
              batchTotals = sp.getString("batchTotals","11111111111")
              Log.e(log,"stan: "+ stan + "," + "batchTotals: " + batchTotals)
              Log.e(log,"send lastSettlement Packet: " + lastSettlementPacket())
              setDialogNormal("","please confirm transaction again.")

          }else{
            setDialogQueryTransaction("","Wait a moment, the system is processing...")
          }

        confirmBtn.setOnClickListener{
            //set settlementFlag = 1

            if(lastSettlementFlag == true){

                sendPacket(lastSettlementPacket())

                val editor: SharedPreferences.Editor = sp.edit()
                editor.putBoolean("lastSettlementFlag", false)
                editor.commit()

            }else{

                stan = readStan?.plus(1)
                batchTotals = buildBatchTotals(saleCount!!, subStringCutZero(saleAmount!!).toDouble())

                val editor: SharedPreferences.Editor = sp.edit()
                editor.putBoolean("settlementFlag", true)
                editor.putString("batchTotals",batchTotals)
                editor.commit()

                //test settlementPacket
                settlementFlag =  sp.getBoolean("settlementFlag",false)
                Log.w(log,"settlementFlag: " + settlementFlag)
                Log.e(log,"stan: "+ stan + "\n" + "batchTotals: " + batchTotals)
                Log.e(log,"Settlement Packet: " + settlementPacket())

                sendPacket(settlementPacket())
            }
        }

        batchBtn.setOnClickListener {


        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)



    }

    override fun onResume() {
        super.onResume()


    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    val SEND_MESSAGE = "send_message"
    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event: MessageEvent){

        if (event.type == "iso_response") {
            manageResponse(event)
        }

    }



    fun sendPacket(packet: ISOMessage?){
        Thread {
            try {
                var client = ISOClientBuilder.createSocket(HOST, PORT)
                    .configureBlocking(false)
                    .setReadTimeout(5000)
                    .build()
                client.connect()

                var response = bytesArrayToHexString(client.sendMessageSync(packet))
                EventBus.getDefault().post(
                    MessageEvent(
                    "iso_response",
                    response.toString())
                )

                client.disconnect()

            } catch (err: ISOClientException) {
                Log.e(log, "error1 is ${err.message}")
                if (err.message.equals("Read Timeout")) {

                    runOnUiThread {
                        setDialog("Transaction failed!.","Response Timeout.")
                    }
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){

                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e(log, "connection fail.")

                    runOnUiThread {
                        setDialog("Transaction failed!.","Unable to connect to bank.")
                    }
                }
            }
        }.start()
    }

    fun manageResponse(event: MessageEvent){

        Log.i("log_tag", "Response Message:" + event.message)
        responseMsg = event.message
        responseCode = codeUnpack(event.message,39)
        Log.e("log_tag", "response code:"+ responseCode)

        if(responseCode == "3030"){

                manageSettlementApprove()

        }else{

            var settlementError  = SaleEntity(null,null,stan)
            var responseSettlementError = ResponseEntity(null,null)

            Thread{

                accessDatabase()
                saleDAO?.insertSale(settlementError)
                responseDAO?.insertResponseMsg(responseSettlementError)
                readStan = saleDAO?.getSale()?.STAN
                readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
//                Log.i("log_tag","saveTransaction :  " + )
                Log.w(log,"saveSTAN : " + readStan)
                Log.w(log,"saveResponse : " + readResponseMsg)

            }.start()


                if(responseCode == "3935"){

                    Log.i(log,"go to batch upload transaction.")

                    val itn =Intent(this,BatchUploadActivity::class.java).apply{
                        putExtra("startId",startId)
                        putExtra("endId",endId)
                    }
                    startActivity(itn)

                } else{

                    errorCode(responseCode,"Please check your problem.")
                    Log.e(log,"Settlement Error!!!.")
                }

        }

    }


    fun manageSettlementApprove(){

        Log.i(log, "Settlement Approve.")

        val editor: SharedPreferences.Editor = sp.edit()
        editor.putBoolean("settlementFlag", false)
        editor.putBoolean("firstTransactionFlag", true)
        editor.putInt("oldStartId", startId!!)
        editor.commit()

        setDialog(null,"Settlement complete.")

        settlementFlag =  sp.getBoolean("settlementFlag",false)
        firstTransactionFlag = sp.getBoolean("firstTransactionFlag",false)
        Log.w(log,"settlementFlag: " + settlementFlag)
        Log.w(log,"firstTransactionFlag: " + firstTransactionFlag)

        var settlementApprove  = SaleEntity(null,null,stan)
        var responseSettlementApprove = ResponseEntity(null,null)

        Thread{

            accessDatabase()
            saleDAO?.insertSale(settlementApprove)
            responseDAO?.insertResponseMsg(responseSettlementApprove)
            readStan = saleDAO?.getSale()?.STAN
            readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
//                Log.i("log_tag","saveTransaction :  " + )
            Log.w(log,"saveSTAN : " + readStan)
            Log.w(log,"saveResponse : " + readResponseMsg)

        }.start()

        //save  sale report in photo album
        bitmap = ScreenShott.getInstance().takeScreenShotOfView(saleReport)
        screenshotTask()
        Log.i(log,"save sale report already.")
    }


    fun accessDatabase(){
        appDatabase = AppDatabase.getAppDatabase(this)
        saleDAO = appDatabase?.saleDao()
        responseDAO = appDatabase?.responseDao()

    }

    fun settlementPacket(): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("920000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(TID))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(MID))
            .setField(FIELDS.F60_Reserved_National,batchNumber)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private,hexStringToByteArray(batchTotals.toString()))
            .setHeader("6001208000")
            .build()
    }

    fun lastSettlementPacket(): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("960000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(TID))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(MID))
            .setField(FIELDS.F60_Reserved_National,batchNumber)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private,hexStringToByteArray(batchTotals.toString()))
            .setHeader("6001208000")
            .build()
    }


    fun setDialogQueryTransaction(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_SHORT).show()

                Thread{
                    //query transaction from DB and sum saleCount and saleAmount
                   accessDatabase()
                    readStan = saleDAO?.getSale()?.STAN
                    readId = saleDAO?.getSale()?._id
                    endId = readId!!

                    Log.w(log, "startId: " + startId)
                    Log.w(log, "endId: " + readId)
                    Log.w(log, "Read STAN: " + readStan)

                    for(i in startId?.rangeTo(endId!!)!!){
//                     for(i in 1..3){
                        readIsoMsg = saleDAO?.getSaleWithID(i)?.isoMsg
                        readResponseMsg = responseDAO?.getResponseMsgWithID(i)?.responseMsg
//                    isoMsgArray.add(readIsoMsg!!)
                        if(readIsoMsg != null){
                            saleCount = saleCount?.plus(1)
                            saleAmount = saleAmount?.plus(codeUnpack(readIsoMsg!!,4)!!.toInt())
                            isoMsgList.add(readIsoMsg!!)

                        }

                        if(readResponseMsg != null){
                            responseCount = responseCount?.plus(1)
//                            responseAmount = responseAmount?.plus(codeUnpack(readResponseMsg!!,4)!!.toInt())
//                            responseAmount = null
                        }

                        Log.e(log, "Read isoMsg: " + readIsoMsg)
                        Log.e(log, "Read responseMsg: " + readResponseMsg)

                     }

                    Log.e(log, "Sale Count: " + saleCount)
                    Log.e(log, "Response Count: " + responseCount)
                    Log.e(log, "Sale Amount: " + saleAmount)
                    Log.e(log, "Response Amount: " + responseAmount)

                    runOnUiThread {

                        saleCountTxt?.setText(saleCount.toString())
                        saleAmountTxt?.setText(subStringCutZero(saleAmount!!).toString())

                        val editor: SharedPreferences.Editor = sp.edit()
                        editor.putString("saleCount", saleCount.toString())
                        editor.putString("saleAmount", subStringCutZero(saleAmount!!).toString())
                        editor.commit()

                    }

//                    accessDatabase()
//                    readStan = saleDAO?.getSale()?.STAN
//                    readFlagReverse = flagReverseDAO?.getFlagReverse()?.flagReverse
//                    readStuckReverse = stuckReverseDAO?.getStuckReverse()?.stuckReverse
//                    reReversal = reversalDAO?.getReversal()?.isoMsg
//                    Log.i("log_tag","readSTAN : " + readStan)
//                    Log.i("log_tag","readFlagReverse : " + readFlagReverse)
//                    Log.i("log_tag","readStuckReverse : " + readStuckReverse)
////                    Log.i("log_tag","reReversal : $reReversal ")
                }.start()
            })

            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.cancel, Toast.LENGTH_LONG).show()
                startActivity(Intent(this,MenuActivity::class.java))
            }

        val dialog = builder.create()
        dialog.show()
    }

    fun reBuildISOPacket(packet: String): ISOMessage? {
        val isoMessage: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(packet)
            .build()
        return isoMessage
    }

    fun errorCode(code: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Transaction Error.")
        builder.setMessage("Error code: " + code +",  ${msg}")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MenuActivity::class.java))
        })
        val dialog = builder.create()
        dialog.show()
    }

    fun setDialogNormal(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
//                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_SHORT).show()
            })
        val dialog = builder.create()
        dialog.show()
    }


    fun setDialog(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
                startActivity(Intent(this,MenuActivity::class.java))
            })
        val dialog = builder.create()
        dialog.show()
    }

    //receipt
//    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE)
    open fun screenshotTask() {
        if (hasStoragePermission()) {
            // Have permissions, do the thing!
            saveScreenshot()
            Toast.makeText(this, "save receipt.", Toast.LENGTH_LONG).show()
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to can write storage.",
                RC_WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        Log.w(log, "requestCode: $requestCode")
        Log.w(log, "permissions: $permissions")
        Log.w(log, "grantResults: $grantResults")
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun saveScreenshot() {
        // Save the screenshot
        try {
            val file: File = ScreenShott.getInstance()
                .saveScreenshotToPicturesFolder(this, bitmap, "receipt")
            // Display a toast
//            Toast.makeText(
//                this, "Bitmap Saved at " + file.absolutePath,
//                Toast.LENGTH_SHORT
//            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bytesArrayToHexString(b1: ByteArray): String? {
        val strBuilder = StringBuilder()
        for (`val` in b1) {
            strBuilder.append(String.format("%02x", `val` and 0xff.toByte()))
        }
        return strBuilder.toString()
    }

    private fun hexStringToByteArray(s: String): ByteArray? {
        val b = ByteArray(s.length / 2)
        for (i in b.indices) {
            val index = i * 2
            val v = s.substring(index, index + 2).toInt(16)
            b[i] = v.toByte()
        }
        return b
    }

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun mtiUnpack(response: String): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val mti: String? = isoMessageUnpacket.getMti()
        return mti
    }



    fun buildBatchTotals(Salecount :Int,Saleamount :Double):String{
        var DE63 =""
        var salecount = Salecount.toString().padStart(3,'0')
        var saleamount = String.format("%.2f",Saleamount)
        var arr : Array<String>
        arr = saleamount.split('.').toTypedArray()
        saleamount = arr[0]+arr[1]
        saleamount = saleamount.padStart(12,'0')
        var data = salecount+saleamount
        data = data.padEnd(90,'0')
        var data_arr :CharArray = data.toCharArray()
        for (i:Int in 0..data.length-1){
            val c = data_arr[i]
            val ascii = c.code
            DE63 += String.format("%02X", ascii)
        }
        return DE63
    }

    fun totalamount(Totalamount : Double ):String{

        var amount : List<String> = String.format("%.2f",Totalamount).split(".")
        var Amount = amount[0]+amount[1]

        return Amount
    }

    fun subStringCutZero(amount : Int):Double{
        var a = amount.toString()
        a = a.substring(0,a.length-2)
        return a.toDouble()
    }


}


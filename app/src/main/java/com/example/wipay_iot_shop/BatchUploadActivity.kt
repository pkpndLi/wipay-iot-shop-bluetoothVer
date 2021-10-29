package com.example.wipay_iot_shop

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.ASSERT
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.AppDatabase
import com.example.testpos.database.transaction.SaleDao
import com.example.testpos.database.transaction.SaleEntity
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.transaction.ResponseDao
import com.example.wipay_iot_shop.transaction.ResponseEntity
import com.imohsenb.ISO8583.builders.ISOClientBuilder
import com.imohsenb.ISO8583.builders.ISOMessageBuilder
import com.imohsenb.ISO8583.entities.ISOMessage
import com.imohsenb.ISO8583.enums.FIELDS
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN
import com.imohsenb.ISO8583.enums.VERSION
import com.imohsenb.ISO8583.exceptions.ISOClientException
import com.imohsenb.ISO8583.exceptions.ISOException
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.util.ArrayList
import kotlin.experimental.and

class BatchUploadActivity : AppCompatActivity() {

    var appDatabase : AppDatabase? = null
    var saleDAO : SaleDao? = null
    var responseDAO : ResponseDao? = null

    // Get SharedPreferences
    private val MY_PREFS = "my_prefs"
    private lateinit var sp: SharedPreferences

    var log = "log"

    var startId:Int? = null
    var endId: Int? = null
    var readIsoMsg: String? = null
    var readResponseMsg:String? = null
    var responseMsg:String? = null
    var readId: Int? = null

    var readStan: Int? = null
    var batchUploadList = ArrayList<String>()

    var amount:String = ""
    var cardNO:String = ""
    var cardEXD:String = ""
    var MTI:String = "0200"
    var time:String = ""
    var date:String = ""
    var responseCode: String = ""
    var saleStan:String = ""
    var batchStan:Int? = 0
    var pccCode: String = "000001"
    var lastPccCode: String = "000000"
    var TID: String = "3232323232323232"
    var MID: String = "323232323232323232323232323232"

    var lastSettlementFlag: Boolean? = null

    var responseCount: Int? = 0
    var batchCount: Int? = 0

    //    private val HOST = "192.168.43.195"
//    var PORT = 5000

//    private val HOST = "192.168.68.195"
//    private val HOST = "192.168.68.225"
//    var PORT = 5000
//    private val HOST = "192.168.68.119"
//    var PORT = 5001

//    private val HOST = "192.168.43.24"
//    var PORT = 3000
    private val HOST = "203.148.160.47"
    var PORT = 7500

//    private val HOST = "192.168.68.107"
//    var PORT = 3000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_upload)

        intent.apply {
            startId = getIntExtra("startId",1)
            endId = getIntExtra("endId",2)
        }

        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
//        startId = sp.getInt("startId",1)

        setDialogQueryTransaction("","Wait a moment, the system is processing...")

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

    }

    override fun onResume() {
        super.onResume()

//        if(trigger == true){
//            manageBatchUpload()
//            trigger = false
//        }


    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event: MessageEvent){

        if(event.type == "sendPacketTrigger"){

            manageBatchUpload()
        }

        if(event.type == "iso_response"){
            manageResponse(event)
        }

    }

    fun manageBatchUpload(){
        //implement batch upload
        Log.i(log,"In manageBatchUpload function.")

        batchCount = 0
        var response: String? = null

        if(batchCount!! < batchUploadList.size){

            sendBatchUploadPacket()

        }

    }

    fun manageResponse(event: MessageEvent){
        Log.i(log,"In manageResponse function.")
//        Log.w(log,"batchCount: " + batchCount)
        Log.w(log, "Response Message:" + event.message)
        responseMsg = event.message
        responseCode = codeUnpack(event.message,39).toString()
        Log.w(log, "response code:"+ responseCode)

        var stan = codeUnpack(responseMsg.toString(),11).toString()
        var batchUpload  = SaleEntity(null,null,stan.toInt())
        var responseBatch = ResponseEntity(null,null)

        Thread{

            accessDatabase()
            saleDAO?.insertSale(batchUpload)
            responseDAO?.insertResponseMsg(responseBatch)
            readStan = saleDAO?.getSale()?.STAN
            readResponseMsg = responseDAO?.getResponseMsg()?.responseMsg
//                Log.i("log_tag","saveTransaction :  " + )
            Log.w(log,"saveSTAN[${batchCount}] : " + readStan)
            Log.w(log,"saveResponse : " + readResponseMsg)

        }.start()

        if(responseCode == "3030"){

            batchCount = batchCount?.plus(1)

            if(batchCount!! < batchUploadList.size){

                sendBatchUploadPacket()

            } else{

                //implement lastSettlement in SettlementActivity
                Log.e(log,"BatchUpload Finish.Back to implement LastSettlement.")
                Log.e(log,"nowStan: " + batchStan)

                val itn =Intent(this,SettlementActivity::class.java).apply{
                    putExtra("lastSettlementFlag",true)
                    putExtra("batchStan",batchStan)
                }
                startActivity(itn)
            }

        }else{
                errorCode(responseCode,"Please check your problem.")
                Log.e(log,"BatchUpload Error!!!.")
        }
    }

    fun sendBatchUploadPacket(){

        Log.i(log,"In sendBatchUploadPacket function.")
        Log.e(log,"send batchUpload packet[${batchCount}]: " + batchUploadList[batchCount!!])
        sendPacket(reBuildISOPacket(batchUploadList[batchCount!!]))
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

    fun reBuildISOPacket(packet: String): ISOMessage? {
        val isoMessage: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(packet)
            .build()
        return isoMessage
    }

    fun buildBatchUploadPacket(){


    }

    fun setDialogQueryTransaction(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_SHORT).show()

                Thread{
//                    buildBatchUploadPacket()
                    accessDatabase()
                    readStan = saleDAO?.getSale()?.STAN
                    readId = saleDAO?.getSale()?._id

//        endId = readId
                    batchStan = readStan

                    if(readStan == null){
                        batchStan = 1
                    }

//                    endId = 3
                    var setPccCode = pccCode
                    var batchUploadPacket: String = ""

                    Log.w(log, "Read STAN: " + readStan)
                    Log.w(log, "startId: " + startId)
                    Log.w(log, "endId: " + endId)

                      for(n in startId?.rangeTo(endId!!)!!){
//                    for(n in startId..endId!!){
                        readResponseMsg = responseDAO?.getResponseMsgWithID(n)?.responseMsg
                        readIsoMsg = saleDAO?.getSaleWithID(n)?.isoMsg

                        Log.w(log,"isoResponseMsg[${n}]: "+ readResponseMsg)
                        Log.w(log,"isoMsg[${n}]: "+ readIsoMsg)
                        if(readResponseMsg != null){

                            responseCount = responseCount?.plus(1)
                            batchStan = batchStan?.plus(1)           //set stan
                            isoUnpackResponse(readResponseMsg.toString())   //set bit 4,12,13,39,11
                            isoUnpackSale(readIsoMsg.toString())            //set bit 2,14

                            if(n == endId){
                                setPccCode = lastPccCode                    //set bit 3
                            }

                            batchUploadPacket = batchUpload(setPccCode).toString()
                            batchUploadList.add(batchUploadPacket)

                            Log.i(log, "batchStan: " + batchStan)
                            Log.i(log, "processingCode: " + setPccCode)
                            Log.e(log, "batchUpload packet[${n}]: " + batchUploadPacket)
                        }

                    }
                    Log.w(log, "Response Count: " + responseCount)
                    Log.w(log,"batchLen: " + batchUploadList.size)

                    EventBus.getDefault().post(MessageEvent(
                        "sendPacketTrigger","true"))

                    runOnUiThread {

                    }

                }.start()
            })

        DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.cancel, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MenuActivity::class.java))
        }

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


    fun batchUpload(pccCode: String): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .fileAction()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Advice, MESSAGE_ORIGIN.Acquirer)
            .processCode(pccCode)
            .setField(FIELDS.F2_PAN,cardNO)
            .setField(FIELDS.F4_AmountTransaction,amount)
            .setField(FIELDS.F11_STAN, batchStan.toString())
            .setField(FIELDS.F12_LocalTime,time)
            .setField(FIELDS.F13_LocalDate,date)
            .setField(FIELDS.F14_ExpirationDate,cardEXD)
            .setField(FIELDS.F22_EntryMode,"0051")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode,"00")
            .setField(FIELDS.F37_RRN,"544553543133303031313234")
            .setField(FIELDS.F38_AuthIdResponse,"323432313339")
            .setField(FIELDS.F39_ResponseCode,responseCode)
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(TID))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray(MID))
            .setField(FIELDS.F60_Reserved_National,hexStringToByteArray(buildDE60_OriginalData(MTI,saleStan)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001208000")
            .build()
    }

    fun isoUnpackResponse(isoMsg: String){
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(isoMsg)
            .build()

        amount = bytesArrayToHexString(isoMessageUnpacket.getField(4)).toString()
        time = bytesArrayToHexString(isoMessageUnpacket.getField(12)).toString()
        date = bytesArrayToHexString(isoMessageUnpacket.getField(13)).toString()
        saleStan = bytesArrayToHexString(isoMessageUnpacket.getField(11)).toString()
        responseCode = bytesArrayToHexString(isoMessageUnpacket.getField(39)).toString()
        Log.i(log,"MTI:" + MTI)
        Log.i(log,"amount:" + amount)
        Log.i(log,"saleStan:" + saleStan)
        Log.i(log,"time:" + time)
        Log.i(log,"date:" + date)
        Log.i(log,"responseCode:" + responseCode)

    }

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun isoUnpackSale(isoMsg: String){
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(isoMsg)
            .build()
        cardNO = bytesArrayToHexString(isoMessageUnpacket.getField(2)).toString()
        cardEXD = bytesArrayToHexString(isoMessageUnpacket.getField(14)).toString()

        Log.i(log,"cardNO:" + cardNO)
        Log.i(log,"cardEXD:" + cardEXD)

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

    fun buildDE60_OriginalData(MTI: String, STAN: String, DE37: String? = null):String {
        var de37 = ""
        if (DE37!=null){
            de37 = DE37
        }
        var DE60 = ""
        var mti = MTI
        var stan = STAN.padStart(6,'0')
        var reserve =""
        var data = mti+stan+de37+reserve.padEnd(12,'0')
        var data_arr :CharArray = data.toCharArray()
        for (i:Int in 0..data.length-1){
            val c = data_arr[i]
            val ascii = c.code
            DE60 += String.format("%02X", ascii)
        }
        return DE60
    }


    fun accessDatabase(){
        appDatabase = AppDatabase.getAppDatabase(this)
        saleDAO = appDatabase?.saleDao()
        responseDAO = appDatabase?.responseDao()

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
}
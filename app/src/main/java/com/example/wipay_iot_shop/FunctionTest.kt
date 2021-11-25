package com.example.wipay_iot_shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.testpos.evenbus.data.MessageEvent
import com.imohsenb.ISO8583.builders.ISOMessageBuilder
import com.imohsenb.ISO8583.entities.ISOMessage
import com.imohsenb.ISO8583.enums.FIELDS
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN
import com.imohsenb.ISO8583.enums.VERSION

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and
import com.imohsenb.ISO8583.utils.StringUtil.hexStringToByteArray

import com.imohsenb.ISO8583.exceptions.ISOClientException

import com.imohsenb.ISO8583.exceptions.ISOException
import com.imohsenb.ISO8583.builders.ISOClientBuilder

import com.imohsenb.ISO8583.interfaces.ISOClient

import com.imohsenb.ISO8583.utils.StringUtil.hexStringToByteArray
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.lang.reflect.InvocationTargetException

class FunctionTest : AppCompatActivity() {

    var log = "log"
    var indicator = "HTLE"
    var version = "04"
    var downlondType = "4"
    var reqType = "1"
    var acqID = "120"
    var LITD = "00000000"
    var vendorID = "12000002"
    var stan = "000007"
    var TE_ID = "12002002"
    var rsaExp = "010001"
    var rsaMod = "8ED7581EA546985DCE653209B5239472B8B6789AB8B4A2E25E8E9F2BECAE8B708FFE62255755FD522BAA39AF5FA0AFF6E75503AD7C051C4AA752FED146D2BC31DCC6C52BA6CE1660FF84496FAFE8FAEDC66EF4475DB087F56EC430B43746A1D8BD9E86BC0BCEEAA1372632B4FEAA245D6ABD1D15EB5B37F669496550082D2613E2FB21BF59EF65202E4732152DEF5284D3227E8A2FAAC1787ECE93A8319C515E272AF35DE6063686AC6E304D44EF4D04BE73C3AF5BDAB32B65E51A8AD3A3E82E70903C3CBB6071254A57586725A08BA8EC2ABCA46D761C8747C0315F076BDE2698F73AF317015566B7F84A267D5230EDD35A05DA2198D8F900A9F65CEC89F7B5"
    var TE_PIN = "22222222"
    var ltmkId = "1369"
    var ltwkId = "0000"
    var tid = "22222222"
    var padding = "1234"
    var pinHash:String  = ""
    var txnHash:String = ""
    var stringHash = ""
    var cardNO = "4162026250958064"
    var cardEXD = "2512"
    var totalAmount = 200

    var strBit62Ltmk:String = ""
    var strBit62Ltwk:String = ""

    private val HOST = "192.168.58.89"
    var PORT = 5000

//    private val HOST = "192.168.43.24"
//    var PORT = 3000

    private val HEX_UPPER = "0123456789ABCDEF".toCharArray()
    private val HEX_LOWER = "0123456789abcdef".toCharArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_function_test)

        var ltmkBtn = findViewById<Button>(R.id.mk)
        var ltwkBtn = findViewById<Button>(R.id.wk)
        var getStanBtn = findViewById<EditText>(R.id.stan)

//        Log.e(log,"convert to hash: " + bytesArrayToHexString(sha1("12002002222222221234")))
//        pinHash = bytesArrayToHexString(sha1("12002002222222221234"))!!.substring(0,8)
//        stringHash = pinHash + LITD + TraceNum.substring(2)
//        Log.e(log,"stringHash: " + stringHash)
//        txnHash = bytesArrayToHexString(sha1(stringHash))!!
//        Log.e(log,"salePacket: " + salePacket().toString())


        txnHash = TXN_Hash(TE_ID,TE_PIN,LITD,stan)
        Log.e(log,"txnHash: " + txnHash)

        strBit62Ltmk = bit62Ltmk(indicator,version,downlondType,reqType,acqID,LITD,vendorID,TE_ID,txnHash,rsaExp,rsaMod)
        Log.e(log,"bit62Ltmk: " + strBit62Ltmk)

        strBit62Ltwk = bit62Ltwk(indicator,version,reqType,acqID,acqID,LITD,vendorID,ltmkId,ltwkId)
        Log.e(log,"bit62Ltwk: " + strBit62Ltwk)
        Log.e(log,"ltwkPacket: " + ltwkPacket())
        Log.e(log,"test bit64 func.: " + bit64Mac(salePacket().toString()))
//        Log.e(log,"salePacketTestMac(): " + salePacketTestMac().toString())
//        Log.e(log,"test bit64 func.: " + bit64Mac(salePacketTestMac().toString()))
//        val input = "java"
//        Log.e(log,"input : $input")
//
//        val hex = convertStringToHex(input, false)
//        Log.e(log,"hex : $hex")

        ltmkBtn.setOnClickListener{

            Log.e(log, "send ltmk")
            Log.e(log, "ltmk msg: " + ltmkPacket())
            var salePacket = salePacket()
            Log.e(log,"salePacketNoMac: " + salePacketNoMac())
            Log.e(log,"salePacket: " + salePacket)
//            Log.e(log,"test MacMsg: " + salePacketTestMac())
//            sendPacket(ltmkPacket())
        }

        ltwkBtn.setOnClickListener{

           Log.e(log, "send ltwk")
           Log.w(log, "ltwk msg: " + ltwkPacket())
           sendPacket(ltwkPacket())
        }
    }

    fun bit64Mac(isoMsg: String): String{

        var preMacMsg = isoMsg.substring(10)
        var data = hexStringToByteArray(preMacMsg)
        Log.e(log,"preMacMsg: " + preMacMsg)
        var arraySize =  if(data?.size?.mod(8) != 0)
            ((data?.size?.div(8))?.plus(1))?.times(8) else
            ((data?.size?.div(8))?.plus(1))?.times(1)

        var _data: ByteArray = ByteArray(arraySize!!)
        System.arraycopy(data,0,_data,0, data?.size!!)
        Log.e(log,"data size: " + data.size)
        Log.e(log,"_data size: " + _data.size)
//        var macData = print(_data)
        var macData = bytesArrayToHexString(_data).toString()
        var _eData = hexStringToByteArray("AAB4EC33BDDA9A9C3EFD8E794C6DACAECA1D4F93012357D360EA2D1A50F3C8C6DFDAB57616F353587AE67E35991554FC0FB772186D971D6D8F0CDA3A002124BC65E4A055E24A3EC3C862155138B1407ACC644A5CCFAF998A")
        var _mac: ByteArray = ByteArray(8)
        System.arraycopy(_eData, _eData?.size!! -8,_mac,0, 4)
        var mac = (bytesArrayToHexString(_mac).toString()).uppercase(Locale.getDefault()) ?: String()
        Log.e(log,"mac is: " + mac)
        return macData
    }

    fun bit57(){

    }

    fun print(bytes: ByteArray): String {
        val sb = java.lang.StringBuilder()
        sb.append("[ ")
        for (b in bytes) {
            sb.append(String.format("0x%02X ", b))
        }
        sb.append("]")
        return sb.toString()
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

    fun manageResponse(event: MessageEvent){

        Log.i("log_tag", "Response Message:" + event.message)
        var responseMsg = event.message

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
                    Log.e(log, "Read Timeout")
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){

                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e(log, "connection fail.")

                }
            }
        }.start()
    }

    fun bit62Ltmk(indicator:String,version:String,downlondType:String,requestType:String,acqID:String,LTID:String,vendorID:String,TEID:String,txnHash:String,rsaExp:String,rsaMod:String):String {

        var str = indicator + version + downlondType + requestType + acqID + LITD + vendorID + TEID + txnHash
        var strToHex = convertStringToHex(str,false)
        var buildBit62 = strToHex + rsaExp + rsaMod

        return buildBit62
    }

   fun bit62Ltwk(indicator:String,version:String,requestType:String,LTMKacqID:String,acqID:String,LTID:String,vendorID:String,ltmkId:String,ltwkId:String):String {

       var str = indicator + version + requestType + LTMKacqID + acqID + LITD + vendorID + ltmkId + ltwkId
       var strToHex: String = convertStringToHex(str,false).toString()

       return strToHex
   }


    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun sha1(str: String): ByteArray = MessageDigest.getInstance("SHA-1").digest(str.toByteArray(UTF_8))


    fun TXN_Hash(TE_ID:String,TE_PIN:String,LITD:String,STAN:String):String{
        var pinHash:String  = ""
        var txnHash:String = ""
        pinHash = bytesArrayToHexString(sha1(TE_ID+TE_PIN+padding))?.uppercase(Locale.getDefault()) ?: String()
        pinHash = pinHash.substring(0,8)
        txnHash = bytesArrayToHexString(sha1(pinHash+LITD+STAN.substring(STAN.length-4,STAN.length)))?.uppercase(Locale.getDefault()) ?: String()
        Log.e(log,"convert to hash: " + txnHash)
        return txnHash.substring(0,8)
    }

    @Throws(ISOException::class, ISOClientException::class, IOException::class,InvocationTargetException::class)
    private fun salePacketNoMac(): String {
          try{
              var isoMsg :ISOMessage = ISOMessageBuilder.Packer(VERSION.V1987)
                  .financial()
                  .setLeftPadding(0x00.toByte())
                  .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
                  .processCode("000000")
                  .setField(FIELDS.F2_PAN, cardNO)
                  .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount.toDouble()))
                  .setField(FIELDS.F11_STAN, stan)
                  .setField(FIELDS.F14_ExpirationDate, cardEXD)
                  .setField(FIELDS.F22_EntryMode, "0010")
                  .setField(FIELDS.F24_NII_FunctionCode, "120")
                  .setField(FIELDS.F25_POS_ConditionCode, "00")
                  .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid,false)))
                  .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
                  .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
                  .setHeader("6001268001")
                  .build()

              return isoMsg.toString()
          }  catch (err: ISOClientException){
              return err.message.toString()

          } catch(err: ISOException){
              return err.message.toString()
          }

    }

    @Throws(ISOException::class, ISOClientException::class, IOException::class)
    fun salePacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("000000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction, convertToFloat(totalAmount.toDouble()))
            .setField(FIELDS.F11_STAN, stan)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001268001")
            .build()

    }

    fun salePacketTestMac(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("003000")
            .setField(FIELDS.F2_PAN, "4830990000183673")
            .setField(FIELDS.F4_AmountTransaction, "000000000556")
            .setField(FIELDS.F11_STAN, "000001")
            .setField(FIELDS.F14_ExpirationDate, "2208")
            .setField(FIELDS.F22_EntryMode, "0022")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F35_Track2,"374830990000183673D22082210000006300000F")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3131313131313131"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("313131313131313131313131313131"))
            .setField(FIELDS.F64_MAC,"")
            .setHeader("6001208000")
            .build()

    }

    private fun ltwkPacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("970400")
            .setField(FIELDS.F11_STAN,stan)
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID, hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray(strBit62Ltwk))
            .setHeader("6001268001")
            .build()
    }

    private fun ltmkPacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("970000")
            .setField(FIELDS.F11_STAN,stan)
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID, hexStringToByteArray(convertStringToHex(tid,false)))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray(strBit62Ltmk))
            .setHeader("6001268001")
            .build()
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


    private fun bytesArrayToHexString(b1: ByteArray): String? {
        val strBuilder = StringBuilder()
        for (`val` in b1) {
            strBuilder.append(String.format("%02x", `val` and 0xff.toByte()))
        }
        return strBuilder.toString()
    }

    fun convertStringToHex(str: String, lowercase: Boolean): String? {
        val HEX_ARRAY: CharArray = if (lowercase) HEX_LOWER else HEX_UPPER
        val bytes = str.toByteArray(UTF_8)

        // two chars form the hex value.
        val hex = CharArray(bytes.size * 2)
        for (j in bytes.indices) {

            // 1 byte = 8 bits,
            // upper 4 bits is the first half of hex
            // lower 4 bits is the second half of hex
            // combine both and we will get the hex value, 0A, 0B, 0C
            val v = (bytes[j] and 0xFF.toByte()).toInt() // byte widened to int, need mask 0xff
            // prevent sign extension for negative number
            hex[j * 2] = HEX_ARRAY[v ushr 4] // get upper 4 bits
            hex[j * 2 + 1] = HEX_ARRAY[v and 0x0F] // get lower 4 bits
        }
        return String(hex)
    }

    fun convertToFloat(Totalamount : Double ):String{
        var amount : List<String> = String.format("%.2f",Totalamount).split(".")
        var Amount = amount[0]+amount[1]
        return Amount
    }


}
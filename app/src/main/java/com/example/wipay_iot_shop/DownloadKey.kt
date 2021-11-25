package com.example.wipay_iot_shop

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.AppDatabase
import com.example.testpos.database.transaction.SaleDao
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.transaction.ResponseDao
import com.imohsenb.ISO8583.builders.ISOClientBuilder
import com.imohsenb.ISO8583.builders.ISOMessageBuilder
import com.imohsenb.ISO8583.entities.ISOMessage
import com.imohsenb.ISO8583.enums.FIELDS
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN
import com.imohsenb.ISO8583.enums.VERSION
import com.imohsenb.ISO8583.exceptions.ISOClientException
import com.imohsenb.ISO8583.exceptions.ISOException
import com.imohsenb.ISO8583.utils.StringUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and

class DownloadKey : AppCompatActivity() {

    var appDatabase : AppDatabase? = null
    var saleDAO : SaleDao? = null
    var responseDAO : ResponseDao? = null

    var log = "log"
    var indicator = "HTLE"
    var version = "04"
    var downlondType = "4"
    var reqType = "1"
    var acqID = "120"
    var LTID = "00000000"
    var vendorID = "12000002"
    var stan:Int = 7
    var TE_ID = "12002002"
    var rsaExp = "010001"
    var rsaMod = "8ED7581EA546985DCE653209B5239472B8B6789AB8B4A2E25E8E9F2BECAE8B708FFE62255755FD522BAA39AF5FA0AFF6E75503AD7C051C4AA752FED146D2BC31DCC6C52BA6CE1660FF84496FAFE8FAEDC66EF4475DB087F56EC430B43746A1D8BD9E86BC0BCEEAA1372632B4FEAA245D6ABD1D15EB5B37F669496550082D2613E2FB21BF59EF65202E4732152DEF5284D3227E8A2FAAC1787ECE93A8319C515E272AF35DE6063686AC6E304D44EF4D04BE73C3AF5BDAB32B65E51A8AD3A3E82E70903C3CBB6071254A57586725A08BA8EC2ABCA46D761C8747C0315F076BDE2698F73AF317015566B7F84A267D5230EDD35A05DA2198D8F900A9F65CEC89F7B5"
    var TE_PIN = "22222222"
    var keyIdLtmk = "1369"
    var keyKCVltmk = ""
    var ltwkId = "0000"
    var tid = "22222222"
    var padding = "1234"
    var pinHash:String  = ""
    var txnHash:String = ""
    var stringHash = ""

    var strBit62Ltmk:String = ""
    var strBit62Ltwk:String = ""

    var responseCode:String = ""

    var ltmkState:Boolean = false
    var ltwkState:Boolean = false

    private val HOST = "192.168.58.89"
    var PORT = 5000

    //    private val HOST = "192.168.43.24"
//    var PORT = 3000

    private val HEX_UPPER = "0123456789ABCDEF".toCharArray()
    private val HEX_LOWER = "0123456789abcdef".toCharArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_key)

        var ltmkBtn = findViewById<Button>(R.id.ltmkBtn)
        var ltwkBtn = findViewById<Button>(R.id.ltwkBtn)
        var androidId: String = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        LTID = androidId

        val unpadded = "7"
        val padded = "000000".substring(unpadded.length) + unpadded

        Log.e(log,"test pedding: " + padded)
        Log.w(log,"test hexToString func." + hexToString("0018496E76616C69642056656E646F725F49442E"))

        Log.w(log,"bit62 to hex: " + hexToString("014D6080010126081020380100028000049700000000012342061115012030303232323232323232029348544C453034343131323047B4B7BB813C3E00ADB1D9F7D05BE635CE2EA36E29152899E4F665EA4F6C3C769AD14E1BAF0858B9034E3C234AE165BE5DC81FF83D6D0C61991F081840A18CDB02803569FB33B05C0FD30CD85E0DF9F4C1D5753AAE7F3FE839A3450721C3E53BDD5F7173221A21D91FAD2FCBC718A7366118F008D3B67685F8192EA64AF776296B93A1969D41E96B52286DAE9C7DB36B01A6FE757DF43B53A5F6158E0120648E915A7DAF9AEF25992D93F74932D0A33DFE615F7F9E387EAA5C76133B8611E12914DDCD3F948F26342933CEDE5C495335C5A1E8E40E00B846960BAA3AC3F1355C3F3EC574629CC0DF3A82C2438F5781BB5B9CF2B1CC7313AEF46DD824620C826E3030364230373231343720202020202020202020202020202020"))
        var getKeyIdMsg = getKeyId("014D6080010126081020380100028000049700000000012342061115012030303232323232323232029348544C453034343131323047B4B7BB813C3E00ADB1D9F7D05BE635CE2EA36E29152899E4F665EA4F6C3C769AD14E1BAF0858B9034E3C234AE165BE5DC81FF83D6D0C61991F081840A18CDB02803569FB33B05C0FD30CD85E0DF9F4C1D5753AAE7F3FE839A3450721C3E53BDD5F7173221A21D91FAD2FCBC718A7366118F008D3B67685F8192EA64AF776296B93A1969D41E96B52286DAE9C7DB36B01A6FE757DF43B53A5F6158E0120648E915A7DAF9AEF25992D93F74932D0A33DFE615F7F9E387EAA5C76133B8611E12914DDCD3F948F26342933CEDE5C495335C5A1E8E40E00B846960BAA3AC3F1355C3F3EC574629CC0DF3A82C2438F5781BB5B9CF2B1CC7313AEF46DD824620C826E3030364230373231343720202020202020202020202020202020")
        var getKeyKCVMsg = getKeyKCV("014D6080010126081020380100028000049700000000012342061115012030303232323232323232029348544C453034343131323047B4B7BB813C3E00ADB1D9F7D05BE635CE2EA36E29152899E4F665EA4F6C3C769AD14E1BAF0858B9034E3C234AE165BE5DC81FF83D6D0C61991F081840A18CDB02803569FB33B05C0FD30CD85E0DF9F4C1D5753AAE7F3FE839A3450721C3E53BDD5F7173221A21D91FAD2FCBC718A7366118F008D3B67685F8192EA64AF776296B93A1969D41E96B52286DAE9C7DB36B01A6FE757DF43B53A5F6158E0120648E915A7DAF9AEF25992D93F74932D0A33DFE615F7F9E387EAA5C76133B8611E12914DDCD3F948F26342933CEDE5C495335C5A1E8E40E00B846960BAA3AC3F1355C3F3EC574629CC0DF3A82C2438F5781BB5B9CF2B1CC7313AEF46DD824620C826E3030364230373231343720202020202020202020202020202020")
        Log.w(log,"test getKeyId func: " + getKeyIdMsg)
        Log.w(log,"test getKeyKCV func: " + getKeyKCVMsg)

        ltmkBtn.setOnClickListener{

            ltmkState = true
            stan = stan.plus(1)
            txnHash = TXN_Hash(TE_ID,TE_PIN,LTID,stan.toString())
            strBit62Ltmk = bit62Ltmk(indicator,version,downlondType,reqType,acqID,LTID,vendorID,TE_ID,txnHash,rsaExp,rsaMod)
            Log.e(log,"txnHash: " + txnHash)
            Log.e(log,"bit62Ltmk: " + strBit62Ltmk)
            Log.e(log, "send ltmk")
            Log.e(log, "ltmk msg: " + ltmkPacket())
//            sendPacket(ltmkPacket())
        }

        ltwkBtn.setOnClickListener{

            ltwkState = true
            stan = stan.plus(1)
            strBit62Ltwk = bit62Ltwk(indicator,version,reqType,acqID,acqID,LTID,vendorID,keyIdLtmk,ltwkId)
            Log.e(log,"bit62Ltwk: " + strBit62Ltwk)
            Log.e(log, "send ltwk")
            Log.w(log, "ltwk msg: " + ltwkPacket())
//            sendPacket(ltwkPacket())
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

    fun manageResponse(event: MessageEvent){

        Log.i(log, "Response Message:" + event.message)
        var responseMsg = event.message
        responseCode = codeUnpack(responseMsg,39).toString()
        Log.e(log, "response code:"+ responseCode)

        if(responseCode == "3030"){

            if(ltmkState == true){

                setNormalDialog("","Download Master Key Success.")
                var bit62Msg = codeUnpack(responseMsg,62).toString()
                keyIdLtmk = getKeyId(bit62Msg)
                keyKCVltmk = getKeyKCV(bit62Msg)

               ltmkState = false

            }else if(ltwkState == true){

                setNormalDialog("","Download Working Key Success.")

               ltwkState = false
            }


        }else{

            var errorMsg = hexToString(codeUnpack(responseMsg,63).toString())
            Log.e(log,"Download Key Error: " + errorMsg)
            setNormalDialog("Download Key Fail.",errorMsg)

        }

    }

    fun accessDatabase(){
        appDatabase = AppDatabase.getAppDatabase(this)
        saleDAO = appDatabase?.saleDao()
        responseDAO = appDatabase?.responseDao()

    }

    private fun ltwkPacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("970400")
            .setField(FIELDS.F11_STAN,stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(
                FIELDS.F41_CA_TerminalID,
                StringUtil.hexStringToByteArray(convertStringToHex(tid, false))
            )
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
            .setField(FIELDS.F11_STAN,stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(
                FIELDS.F41_CA_TerminalID,
                StringUtil.hexStringToByteArray(convertStringToHex(tid, false))
            )
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray(strBit62Ltmk))
            .setHeader("6001268001")
            .build()
    }

    fun getKeyId(bit62 : String):String{
        var keyIdMsg = bit62.substring(630,638)
        var keyId:String = hexToString(keyIdMsg).toString()
        return keyId
    }

    fun getKeyKCV(bit62 : String):String{
        var keyKCVMsg = bit62.substring(618,630)
        var keyKCV:String = hexToString(keyKCVMsg).toString()
        return keyKCV
    }


    fun bit62Ltmk(indicator:String,version:String,downlondType:String,requestType:String,acqID:String,LTID:String,vendorID:String,TEID:String,txnHash:String,rsaExp:String,rsaMod:String):String {

        var str = indicator + version + downlondType + requestType + acqID + LTID + vendorID + TEID + txnHash
        var strToHex = convertStringToHex(str,false)
        var buildBit62 = strToHex + rsaExp + rsaMod

        return buildBit62
    }

    fun bit62Ltwk(indicator:String,version:String,requestType:String,LTMKacqID:String,acqID:String,LTID:String,vendorID:String,ltmkId:String,ltwkId:String):String {

        var str = indicator + version + requestType + LTMKacqID + acqID + LTID + vendorID + ltmkId + ltwkId
        var strToHex: String = convertStringToHex(str,false).toString()

        return strToHex
    }

    fun sha1(str: String): ByteArray = MessageDigest.getInstance("SHA-1").digest(str.toByteArray(
        StandardCharsets.UTF_8
    ))


    fun TXN_Hash(TE_ID:String,TE_PIN:String,LITD:String,STAN:String):String{
        var pinHash:String  = ""
        var txnHash:String = ""
        pinHash = bytesArrayToHexString(sha1(TE_ID+TE_PIN+padding))?.uppercase(Locale.getDefault()) ?: String()
        pinHash = pinHash.substring(0,8)
        var STAN = "000000".substring(STAN.length) + STAN
        txnHash = bytesArrayToHexString(sha1(pinHash+LITD+STAN.substring(STAN.length-4,STAN.length)))?.uppercase(
            Locale.getDefault()) ?: String()
        Log.e(log,"convert to hash: " + txnHash)
        return txnHash.substring(0,8)
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

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun setNormalDialog(title: String?,msg: String?) {
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
        val bytes = str.toByteArray(StandardCharsets.UTF_8)

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

    fun hexToString(hex: String): String? {
        val sb = java.lang.StringBuilder()
        val hexData = hex.toCharArray()
        var count = 0
        while (count < hexData.size - 1) {
            val firstDigit = Character.digit(hexData[count], 16)
            val lastDigit = Character.digit(hexData[count + 1], 16)
            val decimal = firstDigit * 16 + lastDigit
            sb.append(decimal.toChar())
            count += 2
        }
        return sb.toString()
    }


}
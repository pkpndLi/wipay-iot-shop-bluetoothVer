package com.example.wipay_iot_shop

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import com.jf.template.Template
import com.jf.template.impl.CSwiperStateChangedListener
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice
import com.jhl.controller.JHLSwiperController
import java.math.BigDecimal
import java.util.ArrayList

 class ManageBluetooth: AppCompatActivity(), CSwiperStateChangedListener {

     var edit1: EditText? = null
     private val BtnTrace: Spinner? = null
     private var inputAmount: Long = 0
     private val DIALOG = 6

     private val bOpenDevice = false
     private var mMainMessageHandler: Handler? = null
     private var m_editRecvData: EditText? = null
     var deviceName:String = ""

     var controller: JHLSwiperController? = null
     private val TAG = CSwiperStatusListener::class.java.simpleName + ":"

     var totalAmount:Int? = null
     var cardNO:String = ""
     var cardEXD:String = ""
     var menuName:String = ""
     var scanMethod:String = ""

     inner class MessageHandler(looper: Looper?) : Handler(looper!!) {
         private var mLogCount: Long = 0
         override fun handleMessage(msg: Message) {
             when (msg.what) {
                 R.id.editRecvData -> {
                     if (mLogCount > 100) {
                         mLogCount = 0
                         m_editRecvData?.setText("")
                     }
                     val messageString = msg.obj as String
                     val cursor: Int = m_editRecvData!!.getSelectionStart()
                     m_editRecvData?.getText()?.insert(
                         cursor, """
     $messageString
     
     """.trimIndent()
                     )
                     ++mLogCount
                 }
                 R.id.btnAPass -> m_editRecvData?.setText("")
                 0x99 -> {
                     val mDevices = msg.obj as ArrayList<BluetoothIBridgeDevice>
                     val items = arrayOfNulls<String>(mDevices.size)
                     var i = 0
                     while (i < mDevices.size) {
                         items[i] = mDevices[i].deviceName
                         i++
                     }
                     AlertDialog.Builder(this@ManageBluetooth)
                         .setTitle("Please select a Bluetooth device")
                         .setIcon(android.R.drawable.ic_dialog_info)
                         .setSingleChoiceItems(
                             items, 0
                         ) { dialog, which ->
                             dialog.dismiss()
                             showLogMessage("connecting:" + mDevices[which].deviceName)
                             controller?.onConnectBluetooth(30 * 1000, mDevices[which].deviceAddress)
                             deviceName =  mDevices[which].deviceName
                         }
                         .setNegativeButton(
                             "cancel"
                         )  /*null*/
                         { dialog, which -> // TODO Auto-generated method stub
                             dialog.cancel()
                         }
                         .create()
                         .show() //AlertDialog.Builder.create().show()相当于 Dialog.show()
                 }
//                 0x98 -> showDialog(ManageBluetooth.DIALOG)
             }
         }
     }


     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_bluetooth)

         mMainMessageHandler = MessageHandler(Looper.myLooper())
        m_editRecvData = findViewById<View>(R.id.editRecvData) as EditText
        (findViewById<View>(R.id.btnAPass) as Button).setOnClickListener(btnClick)
        (findViewById<View>(R.id.btnANoPass) as Button).setOnClickListener(btnClick)
        (findViewById<View>(R.id.btnNoAPass) as Button).setOnClickListener(btnClick)
        (findViewById<View>(R.id.btnNoANoPass) as Button).setOnClickListener(btnClick)
        (findViewById<View>(R.id.btn_SelectOK) as Button).setOnClickListener(btnClick)

        //CSwiperStatusListener listener = new CSwiperStatusListener();

        //CSwiperStatusListener listener = new CSwiperStatusListener();

         intent.apply {
             totalAmount = getIntExtra("totalAmount",145)
             menuName = getStringExtra("menuName").toString()
             scanMethod = getStringExtra("scanMethod").toString()
         }

//         totalAmount = 20
        controller = JHLSwiperController.getInstance(this)
        controller?.setCSwiperStateChangedListener(this)


         if(controller!!.isConnected){
//             if(deviceName == "BJ00000011"){
                 showLogMessage("Connect device already.")
//             Log.w("log","BTisCon : "+ deviceName)


//             }else{
//                 controller?.disconnectBT()
//             }
         }
         if(!controller!!.isConnected){
//             controller?.scanBTDevice(5)
             showLogMessage("Please connect to the device.")
//         Log.w("log","BT_N_isCon")
         }

    }

     private val btnClick =
         View.OnClickListener { v ->
             when (v.id) {
                 R.id.btnAPass -> {
                     m_editRecvData!!.setText("")
                     if (!controller!!.isConnected) {
                         controller!!.scanBTDevice(5)
                         showLogMessage("Searching Bluetooth Devices...")
                         Log.w("logtag", "Searching Bluetooth Devices...")
                     } else {
                         showLogMessage("Please disconnect the device")
                     }
                 }
                 R.id.btnANoPass -> {
                     m_editRecvData!!.setText("")
                     if (controller!!.isConnected) {
                         showLogMessage("Disconnecting...")
                         controller!!.disconnectBT()
                         Log.w("logtag", "Disconnecting...")
                     } else {
                         showLogMessage("No devices are connected and need not be disconnected.")
                         Log.w("logtag", "No devices are connected and need not be disconnected.")
                     }
                 }
                 R.id.btnNoAPass -> {
                     m_editRecvData!!.setText("")
                     if (controller!!.isConnected) {
                         showLogMessage("Getting KSN...")
                         controller!!.getDeviceInfo()
                     } else {
                         showLogMessage("Please connect to the device...")
                     }
                 }

                 R.id.btnNoANoPass -> {
                     m_editRecvData!!.setText("")
                     if (controller!!.isConnected) {
                         //showLogMessage("请刷卡/插卡/挥卡操作...");
                         createWriteNumberOfMoney() //20190319  弹出金额输入框，再发去交易
                         /*String translog="000001";
                       String orderid="2016070700000001";fdfd
                       controller.setAmount("100", "福店消费", "156", Template.FLAG_PAY_QUERY);
                       controller.onStartCSwiper(3,translog.getBytes(), orderid.getBytes(), 30*1000);
                       */
                     } else {
                         showLogMessage("Please connect to the device...")
                     }
                 }

                 R.id.btn_SelectOK -> {
                     m_editRecvData!!.setText("")

                     val itn = Intent(this,TransactionActivity::class.java).apply{
                         putExtra("totalAmount",totalAmount)
                         putExtra("menuName",menuName)
                         putExtra("cardNO",cardNO)
                         putExtra("cardEXD",cardEXD)
                     }
                     startActivity(itn)
                 }
                 else -> {
                 }
             }
         }


     fun createWriteNumberOfMoney() {
         // TODO Auto-generated method stub
         //对话输入框
//         val factory = LayoutInflater.from(this@ManageBluetooth)
//         val view: View = factory.inflate(R.layout.activity_amount, null)
//         edit1 = view.findViewById<View>(R.id.amount) as EditText
//         edit1!!.addTextChangedListener(textWatcher) //监控金额输入，小数点后2位
//         AlertDialog.Builder(this@ManageBluetooth)
//             .setTitle("Please enter the transaction amount")
//             .setView(view)
//             .setPositiveButton(
//                 "OK"
//             ) { dialog, which ->
//                 var midAmount = ""
//                 val aa = edit1!!.text.toString()
//                 val len = edit1!!.text.toString().length
//                 if (edit1!!.text.toString().length == 0) {
//                     inputAmount = 0
//                     Log.w("logtag","createWriteNumberOfMoney() :  inputAmount = 0")
//                 } else {
//                     val price = edit1!!.text.toString().toLong()
//                     val data1 = BigDecimal(price)
//                     val data2 = BigDecimal(21474836.47)
//                     data1.compareTo(data2)
//                     val ff = data1.compareTo(data2)
//                     if (data1.compareTo(data2) <= 0) {
//                         midAmount = String.format("%.2f", price.toFloat())
////                         midAmount = "%.2f".format(price.toFloat())
//                         val num1 = BigDecimal(midAmount)
//                         val result = num1.multiply(BigDecimal(100))
//                         inputAmount = result.toLong()
//                         Log.w("logtag","createWriteNumberOfMoney() :  inputAmount = result")
//                     } else {
//                         inputAmount = 2147483647
//                     }
//                 }
                 val translog = "000001"
                 val orderid = "2016070700000001"
                 controller!!.setAmount(totalAmount!!.toLong(), "福店消费", "156", Template.FLAG_PAY_QUERY)
                 controller!!.onStartCSwiper(
                     3,
                     translog.toByteArray(),
                     orderid.toByteArray(),
                     30 * 1000
                 )
//             }.setNegativeButton("Cancel", null).create().show()
     }

     var textWatcher: TextWatcher = object : TextWatcher {
         // 输入文本之前的状态
         override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

         // 输入文本中的状态
         override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
             // temp = s; //temp = s   用于记录当前正在输入文本的个数
             //删除.后面超过两位的数字
             var s = s
             if (s.toString().contains(".")) {
                 if (s.length - 1 - s.toString().indexOf(".") > 2) {
                     s = s.toString().subSequence(
                         0,
                         s.toString().indexOf(".") + 3
                     )
                     edit1!!.setText(s)
                     edit1!!.setSelection(s.length)
                 }
             }

             //如果.在起始位置,则起始位置自动补0
             if (s.toString().trim { it <= ' ' }.substring(0) == ".") {
                 s = "0$s"
                 edit1!!.setText(s)
                 edit1!!.setSelection(2)
             }

             //如果起始位置为0并且第二位跟的不是".",则无法后续输入
             if (s.toString().startsWith("0")
                 && s.toString().trim { it <= ' ' }.length > 1
             ) {
                 if (s.toString().substring(1, 2) != ".") {
                     edit1!!.setText(s.subSequence(0, 1))
                     edit1!!.setSelection(1)
                     return
                 }
             }
         }

         override fun afterTextChanged(arg0: Editable) {
             // TODO Auto-generated method stub
         }
     }

     fun Double.format(digits: Int) {

         "%.${digits}f".format(this)
     }

     fun showLogMessage(msg: String?) {
         val updateMessage = mMainMessageHandler!!.obtainMessage()
         updateMessage.obj = msg
         updateMessage.what = R.id.editRecvData
         updateMessage.sendToTarget()
     }

     override fun onDeviceFound(mDevices: ArrayList<BluetoothIBridgeDevice>?) {
//         TODO("Not yet implemented")

         if (Build.VERSION.SDK_INT > 10) {
             if (!isDestroyed) {
                 val ListName = ArrayList<BluetoothIBridgeDevice>()
                 if (mDevices?.size == 0) {
                     showLogMessage("No device found")
                     return
                 }
                 mDevices?.let {
                     synchronized(it) {
                         for (device in mDevices) {
                             var map: String
                             map = if (device.deviceName != null) {
                                 device.deviceName + "=" + device.deviceAddress
                             } else {
                                 "unknown" + "=" + device.deviceAddress
                             }
                             showLogMessage(map)
                             println(map)
                             ListName.add(device)
                         }

                         //弹出选择对话框
                         if (mDevices.size == 0) return
                         val updateMessage = mMainMessageHandler!!.obtainMessage()
                         updateMessage.obj = ListName
                         updateMessage.what = 0x99
                         updateMessage.sendToTarget()
                     }
                 }
             }
         } else {
             Log.d("onDeviceFound", "is Destroyed")
         }
     }

     override fun onAudioDetectStart() {
//         TODO("Not yet implemented")

     }

     override fun onBluetoothBounding() {
//         TODO("Not yet implemented")
         Log.d(TAG + "", "onBluetoothBounding")
         showLogMessage("Connecting...")
     }

     override fun onBluetoothBounded() {
//         TODO("Not yet implemented")
         Log.d(TAG + "", "onBluetoothBounded")
         m_editRecvData!!.setText("")
         showLogMessage("Connecting successful")
     }

     override fun onAudioDetected() {
//         TODO("Not yet implemented")
     }


     override fun onConnectTimeout() {
//         TODO("Not yet implemented")
     }

     override fun onGetKsnCompleted(p0: String?) {
//         TODO("Not yet implemented")
     }

     override fun onWaitingForCardSwipe() {
//         TODO("Not yet implemented")
         Log.d(TAG + "", "onWaitingForCardSwipe")

         if(scanMethod == "swipe"){
             showLogMessage("Please swipe card")
         }else{
             showLogMessage("Please insert card")
         }

     }

     override fun onDetectTrack() {
//         TODO("Not yet implemented")
         Log.d(TAG + "", "onDetectTrack")
     }

     override fun onDetectIC() {
//         TODO("Not yet implemented")
         Log.d(TAG + "", "onDetectIC")
         showLogMessage("IC Card inserted...")
     }

     override fun onDecodingStart() {
//         TODO("Not yet implemented")
         Log.d(TAG + "", "onDecodingStart")
         showLogMessage("In analysis...")
     }

     override fun onDecodeError(p0: Int) {
//         TODO("Not yet implemented")
         Log.d(TAG + "", "onDecodeError")
         showLogMessage("Parsing failed")

     }

     override fun onSwipeCardTimeout() {
//         TODO("Not yet implemented")
         Log.d(TAG + "", "onSwipeCardTimeout")
         showLogMessage("Card swipe timeout!")
     }

//     override fun onDecodeCompleted(
//         String formatID, String ksn,
//         String encTracks, int track1Length, int track2Length,
//         int track3Length, String randomNumber, String maskedPAN,
//         String expiryDate, String cardHolderName, String cardType,
//         String cardMAC, String iccData, boolean isIC, String pinblock,
//         String handBrushWay
//     ) {
//         TODO("Not yet implemented")
//     }

     override fun onDecodeCompleted(
         formatID: String, ksn: String,
         encTracks: String, track1Length: Int, track2Length: Int,
         track3Length: Int, randomNumber: String, maskedPAN: String,
         expiryDate: String, cardHolderName: String, cardType: String,
         cardMAC: String, iccData: String, isIC: Boolean, pinblock: String,
         handBrushWay: String
     ) {
         var strTemp: String

         Log.d(TAG + "", "onDecodeCompleted")
         Log.d(TAG + "", "formatID:$formatID")
         Log.d(TAG + "", "ksn:$ksn")
         Log.d(TAG + "", "encTracks:$encTracks")
         strTemp = String.format("%d", track1Length)
         Log.d(TAG + "", "track1Length:$strTemp")
         strTemp = String.format("%d", track2Length)
         Log.d(TAG + "", "track2Length:$strTemp")
         strTemp = String.format("%d", track3Length)
         Log.d(TAG + "", "track3Length:$strTemp")
         Log.d(TAG + "", "randomNumber:$randomNumber")
         Log.d(TAG + "", "maskedPAN:$maskedPAN")
         Log.d(TAG + "", "expiryDate:$expiryDate")
         Log.d(TAG + "", "cardHolderName:$cardHolderName")
         Log.d(TAG + "", "cardType:$cardType")
         Log.d(TAG + "", "cardMAC:$cardMAC")
         Log.d(TAG + "", "iccData:$iccData")
         if (isIC) Log.d(TAG + "", "isIC:是") else Log.d(TAG + "", "isIC:否")
         Log.d(TAG + "", "pinblock:$pinblock")
         Log.d(TAG + "", "handBrushWay:$handBrushWay")
         //showLogMessage("磁道总参数:"+encTracks+"\r\n"+"一磁道长度："+track1Length+"\r\n"+"cardHolderName:"+cardHolderName+"\r\n"+"二磁道长度:"+track2Length
         //		+"\r\n"+"三磁道长度："+track3Length);
         cardNO = maskedPAN
         cardEXD = expiryDate
         Log.e("logCard","cardNO: " + cardNO)
         Log.e("logCard","cardEXD: " + cardEXD)
         showLogMessage("Decryption succeeded:$maskedPAN")
     }


     override fun onICResponse(p0: Int, p1: ByteArray?, p2: ByteArray?) {
         Log.d(TAG + "", "onICResponse")
     }

     override fun onError(errorCode: Int, error: String?) {
         Log.d(TAG + "", "onError")
         showLogMessage("onError:$error")
     }

     override fun onInterrupted() {
         Log.d(TAG + "", "onInterrupted")
         showLogMessage("Transaction interruption！")
     }

     override fun onTradeCancel() {
         Log.d(TAG + "", "onTradeCancel")
         showLogMessage("Transaction cancellation！")
     }

     override fun onLoadEMVTermConfig(p0: Boolean, p1: Int) {
//         TODO("Not yet implemented")
     }

//     override fun onDestroy() {
//         controller!!.release()
//         try {
//             Thread.sleep(1000)
//         } catch (e: InterruptedException) {
//             // TODO Auto-generated catch block
//             e.printStackTrace()
//         }
//         super.onDestroy()
//     }

 }
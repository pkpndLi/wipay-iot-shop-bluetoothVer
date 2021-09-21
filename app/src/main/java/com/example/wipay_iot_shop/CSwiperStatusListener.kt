package com.example.wipay_iot_shop

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice
import java.util.ArrayList

open class CSwiperStatusListener : AppCompatActivity() {

    private val TAG = CSwiperStatusListener::class.java.simpleName + ":"


    open fun onAudioDetectStart() {
        Log.d(TAG + "", "onAudioDetectStart")
    }

    open fun onBluetoothBounding() {
        Log.d(TAG + "", "onBluetoothBounding")
    }

    open fun onAudioDetected() {
        Log.d(TAG + "", "onAudioDetected")
    }

    open fun onBluetoothBounded() {
        Log.d(TAG + "", "onBluetoothBounded")
    }

    open fun onConnectTimeout() {
        Log.d(TAG + "", "onConnectTimeout")
    }

    open fun onGetKsnCompleted(s: String) {
        Log.d(TAG + "", "onGetKsnCompleted:$s")
    }

    open fun onWaitingForCardSwipe() {
        Log.d(TAG + "", "onWaitingForCardSwipe")
    }

    open fun onDetectTrack() {
        Log.d(TAG + "", "onDetectTrack")
    }

    open fun onDetectIC() {
        Log.d(TAG + "", "onDetectIC")
    }

    open fun onDecodingStart() {
        Log.d(TAG + "", "onDecodingStart")
    }

    open fun onDecodeError(i: Int) {
        Log.d(TAG + "", "onDecodeError")
    }

    open fun onSwipeCardTimeout() {
        Log.d(TAG + "", "onSwipeCardTimeout")
    }

    open fun onDecodeCompleted(
        s: String?,
        s1: String?,
        s2: String?,
        i: Int,
        i1: Int,
        i2: Int,
        s3: String?,
        s4: String?,
        s5: String?,
        s6: String?,
        s7: String?,
        s8: String?,
        s9: String?,
        b: Boolean,
        s10: String?,
        s11: String?
    ) {
        Log.d(TAG + "", "onDecodeCompleted")
    }

    open fun onICResponse(i: Int, bytes: ByteArray?, bytes1: ByteArray?) {
        Log.d(TAG + "", "onICResponse")
    }

    open fun onError(errorCode: Int, error: String?) {
        Log.d(TAG + "", "onError")
    }

    open fun onInterrupted() {
        Log.d(TAG + "", "onInterrupted")
    }

    open fun onTradeCancel() {
        Log.d(TAG + "", "onTradeCancel")
    }

    open fun onDeviceFound(arrayList: ArrayList<BluetoothIBridgeDevice?>?) {}

    open fun onLoadEMVTermConfig(b: Boolean, i: Int) {}
}
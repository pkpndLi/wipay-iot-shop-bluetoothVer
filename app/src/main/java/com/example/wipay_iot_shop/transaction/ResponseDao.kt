package com.example.wipay_iot_shop.transaction

import android.database.Cursor
import androidx.room.*
import com.example.testpos.database.transaction.SaleEntity

@Dao
interface ResponseDao {
    @Query("SELECT * FROM ResponseEntity ORDER BY _id DESC LIMIT 1")
    fun getResponseMsg(): ResponseEntity

    @Query("SELECT * FROM ResponseEntity where _id = :Id")
    fun getResponseMsgWithID(Id: Int) : ResponseEntity

    @Query("SELECT * FROM ResponseEntity")
    fun getAllResponseMsg() : Cursor

    @Insert
    fun insertResponseMsg(saleEntity: ResponseEntity)

    @Delete
    fun deleteResponseMsg(saleEntity: ResponseEntity)

    @Update
    fun updateResponseMsg(saleEntity: ResponseEntity)

}